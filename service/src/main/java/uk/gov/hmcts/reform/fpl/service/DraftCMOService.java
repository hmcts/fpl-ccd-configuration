package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingDateDynamicElement;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.Recital;
import uk.gov.hmcts.reform.fpl.model.common.Schedule;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.configuration.OrderDefinition;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService.EMPTY_PLACEHOLDER;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getLegalAdvisorName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DraftCMOService {
    private static final String CASE_LOCAL_AUTHORITY_PROPERTY_NAME = "caseLocalAuthority";

    private final ObjectMapper mapper;
    private final DateFormatterService dateFormatterService;
    private final DirectionHelperService directionHelperService;
    private final CaseDataExtractionService caseDataExtractionService;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final LocalAuthorityEmailLookupConfiguration localAuthorityEmailLookupConfiguration;
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final OrdersLookupService ordersLookupService;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    public Map<String, Object> extractIndividualCaseManagementOrderObjects(
        CaseManagementOrder caseManagementOrder,
        List<Element<HearingBooking>> hearingDetails) {

        if (isNull(caseManagementOrder)) {
            caseManagementOrder = CaseManagementOrder.builder().build();
        }

        HashMap<String, Object> data = new HashMap<>();
        HashMap<String, Object> reviewCaseManagementOrder = new HashMap<>();

        // TODO: 29/11/2019 Include orderDoc
        reviewCaseManagementOrder.put("cmoStatus", caseManagementOrder.getCmoStatus());

        data.put("cmoHearingDateList", getHearingDateDynamicList(hearingDetails, caseManagementOrder));
        data.put("schedule", caseManagementOrder.getSchedule());
        data.put("recitals", caseManagementOrder.getRecitals());
        data.put("reviewCaseManagementOrder", reviewCaseManagementOrder);

        return data;
    }

    public CaseManagementOrder prepareCMO(Map<String, Object> caseData) {
        DynamicList list = mapper.convertValue(caseData.get("cmoHearingDateList"), DynamicList.class);
        Map<String, Object> reviewCaseManagementOrder = mapper.convertValue(
            caseData.get("reviewCaseManagementOrder"), new TypeReference<>() {});
        CMOStatus cmoStatus = null;
        if (reviewCaseManagementOrder != null) {
            cmoStatus = mapper.convertValue(reviewCaseManagementOrder.get("cmoStatus"), CMOStatus.class);
        }
        Schedule schedule = mapper.convertValue(caseData.get("schedule"), Schedule.class);
        List<Element<Recital>> recitals = mapper.convertValue(caseData.get("recitals"), new TypeReference<>() {});
        DocumentReference orderDoc = mapper.convertValue(caseData.get("orderDoc"), DocumentReference.class);

        return CaseManagementOrder.builder()
            .hearingDate(list.getValue().getLabel())
            .id(list.getValue().getCode())
            .directions(combineAllDirectionsForCmo(mapper.convertValue(caseData, CaseData.class)))
            .schedule(schedule)
            .recitals(recitals)
            .cmoStatus(cmoStatus)
            .orderDoc(orderDoc)
            .build();
    }

    public void prepareCaseDetails(Map<String, Object> caseData, CaseManagementOrder caseManagementOrder) {
        final ImmutableSet<String> keysToRemove = ImmutableSet.of(
            "cmoHearingDateList",
            "schedule",
            "reviewCaseManagementOrder",
            "recitals");

        keysToRemove.forEach(caseData::remove);

        caseData.put("caseManagementOrder", caseManagementOrder);

        switch (caseManagementOrder.getCmoStatus()) {
            case SEND_TO_JUDGE:
                // Does the same as PARTIES_REVIEW for now but in the future this will change
            case PARTIES_REVIEW:
                caseData.put("sharedDraftCMO", caseManagementOrder);
                break;
            case SELF_REVIEW:
                caseData.remove("sharedDraftCMO");
                break;
            default:
                break;
        }
    }

    public DynamicList buildDynamicListFromHearingDetails(List<Element<HearingBooking>> hearingDetails) {
        List<HearingDateDynamicElement> hearingDates = hearingDetails
            .stream()
            .map(element -> new HearingDateDynamicElement(
                formatLocalDateToMediumStyle(element.getValue().getStartDate().toLocalDate()), element.getId()))
            .collect(toList());

        return DynamicList.toDynamicList(hearingDates, DynamicListElement.EMPTY);
    }

    private DynamicList getHearingDateDynamicList(List<Element<HearingBooking>> hearingDetails,
                                                  CaseManagementOrder caseManagementOrder) {
        DynamicList hearingDatesDynamic = buildDynamicListFromHearingDetails(hearingDetails);

        if (isNotEmpty(caseManagementOrder)) {
            prePopulateHearingDateSelection(hearingDetails, hearingDatesDynamic, caseManagementOrder);
        }

        return hearingDatesDynamic;
    }

    public String createRespondentAssigneeDropdownKey(List<Element<Respondent>> respondents) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < respondents.size(); i++) {
            RespondentParty respondentParty = respondents.get(i).getValue().getParty();

            String key = String.format("Respondent %d - %s", i + 1, getRespondentFullName(respondentParty));
            stringBuilder.append(key).append("\n\n");
        }

        return stringBuilder.toString().stripTrailing();
    }

    public String createOtherPartiesAssigneeDropdownKey(Others others) {
        StringBuilder stringBuilder = new StringBuilder();

        if (isNotEmpty(others)) {
            for (int i = 0; i < others.getAllOthers().size(); i++) {
                Other other = others.getAllOthers().get(i);
                String key;

                if (i == 0) {
                    key = String.format("Person 1 - %s", defaultIfNull(other.getName(), EMPTY_PLACEHOLDER));
                } else {
                    key = String.format("Other Person %d - %s", i,
                        defaultIfNull(other.getName(), EMPTY_PLACEHOLDER));
                }

                stringBuilder.append(key).append("\n\n");
            }
        }

        return stringBuilder.toString().stripTrailing();
    }

    public void prepareCustomDirections(Map<String, Object> data) {
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        if (!isNull(caseData.getCaseManagementOrder())) {
            directionHelperService.sortDirectionsByAssignee(caseData.getCaseManagementOrder().getDirections())
                .forEach((key, value) -> data.put(key.getValue(), value));
        } else {
            removeExistingCustomDirections(data);
        }
    }

    // REFACTOR: 02/12/2019 Refactor this with CaseDataExtractionService and NotifyOfProceedingService to try and
    //  extract common elements to CommonCaseDataExtractionService (maybe an separate service for docmosis templates?)
    @SuppressWarnings("unchecked")
    public Map<String, Object> generateCMOTemplateData(Map<String, Object> caseDataMap) throws IOException {
        // TODO: 30/11/2019 TestMe
        ImmutableMap.Builder cmoTemplateData = ImmutableMap.<String, Object>builder();

        DynamicList hearingDateList = mapper.convertValue(caseDataMap.get("cmoHearingDateList"), DynamicList.class);
        CaseData caseData = mapper.convertValue(caseDataMap, CaseData.class);
        CaseManagementOrder caseManagementOrder = prepareCMO(caseDataMap);

        cmoTemplateData.put("familyManCaseNumber", defaultIfNull(caseData.getFamilyManCaseNumber(), EMPTY_PLACEHOLDER));
        cmoTemplateData.put("generationDate",
            dateFormatterService.formatLocalDateToString(LocalDate.now(), FormatStyle.LONG));
        cmoTemplateData.put("complianceDeadline", caseData.getDateSubmitted() != null
            ? dateFormatterService.formatLocalDateToString(caseData.getDateSubmitted().plusWeeks(26),
            FormatStyle.LONG) : EMPTY_PLACEHOLDER);

        List<Map<String, String>> childrenInCase = caseDataExtractionService.getChildrenDetails(caseData);
        cmoTemplateData.put("children", childrenInCase);
        cmoTemplateData.put("numberOfChildren", childrenInCase.size());

        cmoTemplateData.put("courtName", defaultIfBlank(
            hmctsCourtLookupConfiguration.getCourt(caseData.getCaseLocalAuthority()).getName(), EMPTY_PLACEHOLDER));

        cmoTemplateData.put("applicantName", caseDataExtractionService.getFirstApplicantName(caseData));

        List<Map<String, String>> respondentsNameAndRelationship =
            caseDataExtractionService.getRespondentsNameAndRelationship(caseData);
        cmoTemplateData.put("respondents", respondentsNameAndRelationship);
        cmoTemplateData.put("respondentsProvided", !respondentsNameAndRelationship.isEmpty());

        String localAuthorityCode = (String) caseDataMap.get(CASE_LOCAL_AUTHORITY_PROPERTY_NAME);
        cmoTemplateData.put("localAuthoritySolicitorEmail", localAuthorityEmailLookupConfiguration
            .getLocalAuthority(localAuthorityCode)
            .map(LocalAuthorityEmailLookupConfiguration.LocalAuthority::getEmail)
            .orElse(""));

        cmoTemplateData.put("localAuthorityName", defaultIfBlank(
                localAuthorityNameLookupConfiguration.getLocalAuthorityName(localAuthorityCode),
                EMPTY_PLACEHOLDER));

        // defaulting to EMPTY_PLACEHOLDER for now as we currently do not capture
        cmoTemplateData.put("localAuthoritySolicitorName", EMPTY_PLACEHOLDER);
        cmoTemplateData.put("localAuthoritySolicitorPhoneNumber", EMPTY_PLACEHOLDER);

        cmoTemplateData.put("respondentOneName", getFirstRespondentFullName(caseData));

        cmoTemplateData.putAll(getEmptyHearingBookingData());

        HearingBooking hearingBooking = getHearingBooking(caseData, hearingDateList);
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = hearingBooking.getJudgeAndLegalAdvisor();
        //EMPTY_PLACEHOLDER given template definition
        cmoTemplateData.put("judgeTitle", EMPTY_PLACEHOLDER);
        cmoTemplateData.put("judgeName", defaultString(judgeAndLegalAdvisor.getJudgeLastName(),
            EMPTY_PLACEHOLDER));
        cmoTemplateData.put("legalAdvisorName", defaultString(getLegalAdvisorName(
            judgeAndLegalAdvisor), EMPTY_PLACEHOLDER));

        cmoTemplateData.putAll(getGroupedCMODirections(caseManagementOrder));

        cmoTemplateData.put("draftbackground", String.format("image:base64:%1$s",
            docmosisDocumentGeneratorService.generateDraftWatermarkEncodedString()));

        List<Map<String, String>> recitals = buildRecitals(caseManagementOrder.getRecitals());
        cmoTemplateData.put("recitals", recitals);
        cmoTemplateData.put("recitalsProvided", isNotEmpty(recitals));

        cmoTemplateData.putAll(buildSchedule(caseManagementOrder.getSchedule()));

        cmoTemplateData.put("scheduleProvided", isNotEmpty(caseManagementOrder.getSchedule()));

        //defaulting as 1 as we currently do not have impl for multiple CMos
        cmoTemplateData.put("caseManagementNumber", 1);

        return cmoTemplateData.build();
    }

    private Map<String, List<Map<String, String>>> getGroupedCMODirections(
        final CaseManagementOrder caseManagementOrder) throws IOException {
        OrderDefinition caseManagementOrderDefinition = ordersLookupService.getDirectionOrder();

        if (isNull(caseManagementOrder)) {
            return ImmutableMap.of();
        }

        Map<DirectionAssignee, List<Element<Direction>>> groupedDirections =
            directionHelperService.sortDirectionsByAssignee(directionHelperService.numberDirections(
                caseManagementOrder.getDirections()));

        ImmutableMap.Builder<String, List<Map<String, String>>> formattedDirections = ImmutableMap.builder();

        groupedDirections.forEach((key, value) -> {
            List<Map<String, String>> directionsList = value.stream()
                .map(Element::getValue)
                .filter(direction -> !"No".equals(direction.getDirectionNeeded()))
                .map(direction -> ImmutableMap.of(
                    "title", caseDataExtractionService.formatTitle(
                        direction, caseManagementOrderDefinition.getDirections()),
                    "body", defaultIfNull(direction.getDirectionText(), EMPTY_PLACEHOLDER)))
                .collect(toList());

            formattedDirections.put(key.getValue(), directionsList);
        });

        return formattedDirections.build();
    }

    private String getFirstRespondentFullName(final CaseData caseData) {
        return caseData.buildFirstRespondentFullName();
    }

    private HearingBooking getHearingBooking(final CaseData caseData, DynamicList hearingDateList) {
        return caseData.getHearingDetails()
            .stream()
            .filter(element -> element.getId().equals(hearingDateList.getValue().getCode()))
            .findFirst()
            .map(Element::getValue)
            .orElse(HearingBooking.builder().build());
    }

    public Map<String, Object> getEmptyHearingBookingData() {
        //passing in EMPTY_PLACEHOLDER following template definition
        return ImmutableMap.of(
            "hearingDate", EMPTY_PLACEHOLDER,
            "hearingVenue", EMPTY_PLACEHOLDER,
            "preHearingAttendance", EMPTY_PLACEHOLDER,
            "hearingTime", EMPTY_PLACEHOLDER
        );
    }

    private Map<String, String> buildSchedule(final Schedule schedule) {
        if (schedule == null) {
            return ImmutableMap.of();
        }
        return Map.of("allocation", defaultString(schedule.getAllocation(), EMPTY_PLACEHOLDER),
            "application", defaultString(schedule.getApplication(), EMPTY_PLACEHOLDER),
            "todaysHearing", defaultString(schedule.getTodaysHearing(), EMPTY_PLACEHOLDER),
            "childrensCurrentArrangement", defaultString(schedule.getChildrensCurrentArrangement(), EMPTY_PLACEHOLDER),
            "timetableForProceedings", defaultString(schedule.getTimetableForProceedings(), EMPTY_PLACEHOLDER),
            "timetableForChildren", defaultString(schedule.getTimetableForChildren(), EMPTY_PLACEHOLDER),
            "alternativeCarers", defaultString(schedule.getAlternativeCarers(), EMPTY_PLACEHOLDER),
            "threshold", defaultString(schedule.getThreshold(), EMPTY_PLACEHOLDER),
            "keyIssues", defaultString(schedule.getKeyIssues(), EMPTY_PLACEHOLDER),
            "partiesPositions", defaultString(schedule.getPartiesPositions(), EMPTY_PLACEHOLDER));
    }

    private List<Map<String, String>> buildRecitals(final List<Element<Recital>> recitals) {
        if (isEmpty(recitals)) {
            return emptyList();
        }

        return recitals.stream().filter(Objects::nonNull)
            .map(Element::getValue)
            .map(recital -> ImmutableMap.of("title", defaultString(recital.getTitle(), EMPTY_PLACEHOLDER),
                "body", defaultString(recital.getDescription(), EMPTY_PLACEHOLDER)))
            .collect(Collectors.toList());
    }

    private void removeExistingCustomDirections(Map<String, Object> caseData) {
        caseData.remove("allPartiesCustom");
        caseData.remove("localAuthorityDirectionsCustom");
        caseData.remove("cafcassDirectionsCustom");
        caseData.remove("courtDirectionsCustom");
        caseData.remove("respondentDirectionsCustom");
        caseData.remove("otherPartiesDirectionsCustom");
    }

    private void prePopulateHearingDateSelection(List<Element<HearingBooking>> hearingDetails,
                                                 DynamicList hearingDatesDynamic,
                                                 CaseManagementOrder caseManagementOrder) {
        UUID hearingDateId = caseManagementOrder.getId();
        // There was a previous hearing date therefore we need to remap it
        String date = hearingDetails.stream()
            .filter(Objects::nonNull)
            .filter(element -> element.getId().equals(caseManagementOrder.getId()))
            .findFirst()
            .map(element -> formatLocalDateToMediumStyle(element.getValue().getStartDate().toLocalDate()))
            .orElse("");

        DynamicListElement listElement = DynamicListElement.builder()
            .label(date)
            .code(hearingDateId)
            .build();

        hearingDatesDynamic.setValue(listElement);
    }

    private String getRespondentFullName(RespondentParty respondentParty) {
        String firstName = defaultString(respondentParty.getFirstName());
        String lastName = defaultString(respondentParty.getLastName());
        return String.format("%s %s", firstName, lastName);
    }

    private List<Element<Direction>> combineAllDirectionsForCmo(CaseData caseData) {
        List<Element<Direction>> directions = new ArrayList<>();

        directions.addAll(directionHelperService.assignCustomDirections(caseData.getAllPartiesCustom(), ALL_PARTIES));

        directions.addAll(directionHelperService.assignCustomDirections(caseData.getLocalAuthorityDirectionsCustom(),
            LOCAL_AUTHORITY));

        directions.addAll(directionHelperService.assignCustomDirections(caseData.getCafcassDirectionsCustom(),
            CAFCASS));

        directions.addAll(directionHelperService.assignCustomDirections(caseData.getCourtDirectionsCustom(), COURT));

        directions.addAll(directionHelperService.assignCustomDirections(caseData.getRespondentDirectionsCustom(),
            PARENTS_AND_RESPONDENTS));

        directions.addAll(directionHelperService.assignCustomDirections(caseData.getOtherPartiesDirectionsCustom(),
            OTHERS));

        return directions;
    }

    private String formatLocalDateToMediumStyle(LocalDate date) {
        return dateFormatterService.formatLocalDateToString(date, FormatStyle.MEDIUM);
    }
}
