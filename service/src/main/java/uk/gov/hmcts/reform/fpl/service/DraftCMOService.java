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
import uk.gov.hmcts.reform.fpl.enums.OtherPartiesDirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.ParentsAndRespondentsDirectionAssignee;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService.EMPTY_PLACEHOLDER;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getLegalAdvisorName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DraftCMOService {
    private final ObjectMapper mapper;
    private final DateFormatterService dateFormatterService;
    private final DirectionHelperService directionHelperService;
    private final CaseDataExtractionService caseDataExtractionService;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final LocalAuthorityEmailLookupConfiguration localAuthorityEmailLookupConfiguration;
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final OrdersLookupService ordersLookupService;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final CommonCaseDataExtractionService commonCaseDataExtractionService;

    public Map<String, Object> extractIndividualCaseManagementOrderObjects(
        CaseManagementOrder caseManagementOrder,
        List<Element<HearingBooking>> hearingDetails) {

        if (isNull(caseManagementOrder)) {
            caseManagementOrder = CaseManagementOrder.builder().build();
        }

        HashMap<String, Object> data = new HashMap<>();
        HashMap<String, Object> reviewCaseManagementOrder = new HashMap<>();

        reviewCaseManagementOrder.put("cmoStatus", caseManagementOrder.getCmoStatus());
        reviewCaseManagementOrder.put("orderDoc", caseManagementOrder.getOrderDoc());

        data.put("cmoHearingDateList", getHearingDateDynamicList(hearingDetails, caseManagementOrder));
        data.put("schedule", caseManagementOrder.getSchedule());
        data.put("recitals", caseManagementOrder.getRecitals());
        data.put("reviewCaseManagementOrder", reviewCaseManagementOrder);

        return data;
    }

    public CaseManagementOrder prepareCMO(Map<String, Object> caseData) {
        DynamicList list = mapper.convertValue(caseData.get("cmoHearingDateList"), DynamicList.class);

        String hearingDate = null;
        UUID id = null;

        if (list != null) {
            hearingDate = list.getValue().getLabel();
            id = list.getValue().getCode();
        }

        Map<String, Object> reviewCaseManagementOrder = mapper.convertValue(
            caseData.get("reviewCaseManagementOrder"), new TypeReference<>() {});
        CMOStatus cmoStatus = null;
        DocumentReference orderDoc = null;

        if (reviewCaseManagementOrder != null) {
            cmoStatus = mapper.convertValue(reviewCaseManagementOrder.get("cmoStatus"), CMOStatus.class);
            orderDoc = mapper.convertValue(reviewCaseManagementOrder.get("orderDoc"), DocumentReference.class);
        }

        Schedule schedule = mapper.convertValue(caseData.get("schedule"), Schedule.class);
        List<Element<Recital>> recitals = mapper.convertValue(caseData.get("recitals"), new TypeReference<>() {});

        return CaseManagementOrder.builder()
            .hearingDate(hearingDate)
            .id(id)
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

            String key = String.format("Respondent %d - %s", i + 1, respondentParty.getFullName());
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
        ImmutableMap.Builder cmoTemplateData = ImmutableMap.<String, Object>builder();

        DynamicList hearingDateList = mapper.convertValue(caseDataMap.get("cmoHearingDateList"), DynamicList.class);
        CaseData caseData = mapper.convertValue(caseDataMap, CaseData.class);
        String localAuthorityCode = caseData.getCaseLocalAuthority();
        CaseManagementOrder caseManagementOrder = defaultIfNull(prepareCMO(caseDataMap),
            CaseManagementOrder.builder().build());

        cmoTemplateData.put("familyManCaseNumber", defaultIfNull(caseData.getFamilyManCaseNumber(), EMPTY_PLACEHOLDER));
        cmoTemplateData.put("generationDate",
            dateFormatterService.formatLocalDateToString(LocalDate.now(), FormatStyle.LONG));
        cmoTemplateData.put("complianceDeadline", caseData.getDateSubmitted() != null
            ? dateFormatterService.formatLocalDateToString(caseData.getDateSubmitted().plusWeeks(26),
            FormatStyle.LONG) : EMPTY_PLACEHOLDER);

        List<Map<String, String>> childrenInCase = caseDataExtractionService.getChildrenDetails(caseData);
        cmoTemplateData.put("children", childrenInCase);
        cmoTemplateData.put("numberOfChildren", childrenInCase.size());

        cmoTemplateData.put("courtName", getCourtName(localAuthorityCode));

        cmoTemplateData.put("applicantName", caseDataExtractionService.getFirstApplicantName(caseData));

        List<Map<String, String>> respondentsNameAndRelationship = caseDataExtractionService
            .getRespondentsNameAndRelationship(caseData);

        cmoTemplateData.put("respondents", respondentsNameAndRelationship);

        cmoTemplateData.put("representatives", getRepresentatives(caseData.getRespondents1(), localAuthorityCode));

        // Populate with the next hearing booking, currently not captured
        cmoTemplateData.putAll(commonCaseDataExtractionService.getHearingBookingData(null));

        HearingBooking hearingBooking = getHearingBooking(caseData.getHearingDetails(), hearingDateList);
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = hearingBooking.getJudgeAndLegalAdvisor();
        cmoTemplateData.put("judgeTitleAndName", defaultIfBlank(formatJudgeTitleAndName(judgeAndLegalAdvisor),
            EMPTY_PLACEHOLDER));
        cmoTemplateData.put("legalAdvisorName", defaultIfBlank(getLegalAdvisorName(
            judgeAndLegalAdvisor), EMPTY_PLACEHOLDER));

        cmoTemplateData.putAll(getGroupedCMODirections(caseManagementOrder));

        cmoTemplateData.put("draftbackground", String.format("image:base64:%1$s",
            docmosisDocumentGeneratorService.generateDraftWatermarkEncodedString()));

        List<Map<String, String>> recitals = buildRecitals(caseManagementOrder.getRecitals());
        cmoTemplateData.put("recitals", recitals);
        cmoTemplateData.put("recitalsProvided", isNotEmpty(recitals));

        cmoTemplateData.putAll(getSchedule(caseManagementOrder));

        //defaulting as 1 as we currently do not have impl for multiple CMos
        cmoTemplateData.put("caseManagementNumber", 1);

        return cmoTemplateData.build();
    }

    private List<Map<String, Object>> getRepresentatives(List<Element<Respondent>> respondents1,
                                                         String localAuthorityCode) {
        List<Map<String, Object>> representatives = new ArrayList<>();

        representatives.add(getLocalAuthorityDetails(localAuthorityCode));

        if (!isEmpty(respondents1)) {
            respondents1.stream()
                .map(Element::getValue)
                .forEach(respondent -> representatives.add(getRepresentative(respondent)));
        }

        return representatives;
    }

    private Map<String, Object> getRepresentative(Respondent respondent) {
        return ImmutableMap.of(
            "respondentName", respondent.getParty().getFullName(),
            "representativeName", EMPTY_PLACEHOLDER,
            "representativeEmail", EMPTY_PLACEHOLDER,
            "representativePhoneNumber", EMPTY_PLACEHOLDER
        );
    }

    private Map<String, Object> getSchedule(CaseManagementOrder caseManagementOrder) {
        final Schedule schedule = caseManagementOrder.getSchedule();
        Map<String, Object> scheduleMap = mapper.convertValue(schedule, new TypeReference<>() {});
        Map<String, Object> result;

        if ((schedule == null) || schedule.getIncludeSchedule().equals("No")) {
            result = getEmptyScheduleMap();
        } else {
            result = scheduleMap;
        }

        result.put("scheduleProvided", schedule != null && "Yes".equals(schedule.getIncludeSchedule()));
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getEmptyScheduleMap() {
        final String[] scheduleKeys = {
            "includeSchedule", "allocation", "application", "todaysHearing", "childrensCurrentArrangement",
            "timetableForProceedings", "timetableForChildren", "alternativeCarers", "threshold", "keyIssues",
            "partiesPositions"
        };

        Map<String, Object> scheduleMap = new LinkedHashMap<>();

        Arrays.stream(scheduleKeys).forEach(key -> scheduleMap.put(key, EMPTY_PLACEHOLDER));

        return scheduleMap;
    }

    private Map<String, Object> getLocalAuthorityDetails(String localAuthorityCode) {
        if (isBlank(localAuthorityCode)) {
            return ImmutableMap.of(
                "respondentName", EMPTY_PLACEHOLDER,
                "representativeName", EMPTY_PLACEHOLDER,
                "representativeEmail", EMPTY_PLACEHOLDER,
                "representativePhoneNumber", EMPTY_PLACEHOLDER
            );
        }

        return ImmutableMap.of(
            "respondentName", defaultIfBlank(
                localAuthorityNameLookupConfiguration.getLocalAuthorityName(localAuthorityCode),
                EMPTY_PLACEHOLDER),
            "representativeName", EMPTY_PLACEHOLDER,
            "representativeEmail", localAuthorityEmailLookupConfiguration.getLocalAuthority(localAuthorityCode)
                    .map(LocalAuthorityEmailLookupConfiguration.LocalAuthority::getEmail)
                    .orElse(EMPTY_PLACEHOLDER),
            // defaulting to EMPTY_PLACEHOLDER for now as we currently do not capture
            "representativePhoneNumber", EMPTY_PLACEHOLDER);

    }

    private String getCourtName(String localAuthorityCode) {
        if (isBlank(localAuthorityCode)) {
            return EMPTY_PLACEHOLDER;
        }
        return defaultIfBlank(hmctsCourtLookupConfiguration.getCourt(localAuthorityCode).getName(), EMPTY_PLACEHOLDER);
    }

    private Map<String, Object> getGroupedCMODirections(
        final CaseManagementOrder caseManagementOrder) throws IOException {

        OrderDefinition caseManagementOrderDefinition = ordersLookupService.getDirectionOrder();

        if (caseManagementOrder == null || isEmpty(caseManagementOrder.getDirections())) {
            return ImmutableMap.of();
        }

        Map<DirectionAssignee, List<Element<Direction>>> groupedDirections =
            directionHelperService.sortDirectionsByAssignee(directionHelperService.numberDirections(
                caseManagementOrder.getDirections()));

        List<Element<Direction>> parentsAndRespondents = groupedDirections.remove(PARENTS_AND_RESPONDENTS);
        List<Element<Direction>> otherParties = groupedDirections.remove(OTHERS);
        ImmutableMap.Builder<String, Object> formattedDirections = ImmutableMap.builder();

        final Map<ParentsAndRespondentsDirectionAssignee, List<Element<Direction>>> groupedParentsAndRespondents =
            parentsAndRespondents.stream()
                .collect(groupingBy(directionElement -> directionElement.getValue()
                    .getParentsAndRespondentsAssignee()));

        final Map<OtherPartiesDirectionAssignee, List<Element<Direction>>> groupedOtherParties = otherParties.stream()
            .collect(groupingBy(directionElement -> directionElement.getValue().getOtherPartiesAssignee()));

        formattedDirections.put(PARENTS_AND_RESPONDENTS.getValue(),
            getFormattedParentsAndRespondentsDirections(caseManagementOrderDefinition, groupedParentsAndRespondents));

        formattedDirections.put(OTHERS.getValue(), getFormattedOtherPartiesDirections(
            caseManagementOrderDefinition, groupedOtherParties));

        groupedDirections.forEach((key, value) -> {
            List<Map<String, String>> directionsList = buildFormattedDirectionList(
                caseManagementOrderDefinition, value);
            formattedDirections.put(key.getValue(), directionsList);
        });

        return formattedDirections.build();
    }

    private List<Map<String, Object>> getFormattedOtherPartiesDirections(
        OrderDefinition caseManagementOrderDefinition,
        Map<OtherPartiesDirectionAssignee, List<Element<Direction>>> groupedOtherParties) {

        List<Map<String, Object>> directionsToRespondents = new ArrayList<>();
        groupedOtherParties.forEach((key, value) -> {
            Map<String, Object> directionForRespondent = new HashMap<>();
            directionForRespondent.put("header", "For " + key.getLabel());
            List<Map<String, String>> directionsList = buildFormattedDirectionList(
                caseManagementOrderDefinition, value);
            directionForRespondent.put("directions", directionsList);
            directionsToRespondents.add(directionForRespondent);
        });

        return directionsToRespondents;
    }


    private List<Map<String, Object>> getFormattedParentsAndRespondentsDirections(
        OrderDefinition caseManagementOrderDefinition,
        Map<ParentsAndRespondentsDirectionAssignee, List<Element<Direction>>> groupedParentsAndRespondents) {

        List<Map<String, Object>> directionsToRespondents = new ArrayList<>();
        groupedParentsAndRespondents.forEach((key, value) -> {
            Map<String, Object> directionForRespondent = new HashMap<>();
            directionForRespondent.put("header", "For " + key.getLabel());
            List<Map<String, String>> directionsList = buildFormattedDirectionList(
                caseManagementOrderDefinition, value);
            directionForRespondent.put("directions", directionsList);
            directionsToRespondents.add(directionForRespondent);
        });

        return directionsToRespondents;
    }

    private List<Map<String, String>> buildFormattedDirectionList(OrderDefinition caseManagementOrderDefinition,
                                                                  List<Element<Direction>> directions) {
        return directions.stream()
            .map(Element::getValue)
            .filter(direction -> !"No".equals(direction.getDirectionNeeded()))
            .map(direction -> ImmutableMap.of(
                "title", caseDataExtractionService.formatTitle(
                    direction, caseManagementOrderDefinition.getDirections()),
                "body", defaultIfNull(direction.getDirectionText(), EMPTY_PLACEHOLDER)))
            .collect(toList());
    }

    private HearingBooking getHearingBooking(final List<Element<HearingBooking>> hearingDetails,
                                             final DynamicList hearingDateList) {
        if (hearingDetails == null) {
            return HearingBooking.builder().build();
        }

        return hearingDetails.stream()
            .filter(element -> element.getId().equals(hearingDateList.getValue().getCode()))
            .findFirst()
            .map(Element::getValue)
            .orElse(HearingBooking.builder().build());
    }

    private List<Map<String, String>> buildRecitals(final List<Element<Recital>> recitals) {
        if (isEmpty(recitals)) {
            return emptyList();
        }

        return recitals.stream()
            .filter(Objects::nonNull)
            .map(Element::getValue)
            .map(recital -> ImmutableMap.of(
                "title", defaultString(recital.getTitle(), EMPTY_PLACEHOLDER),
                "body", defaultString(recital.getDescription(), EMPTY_PLACEHOLDER)
            ))
            .collect(toList());
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

    private List<Element<Direction>> combineAllDirectionsForCmo(CaseData caseData) {
        List<Element<Direction>> directions = new ArrayList<>();

        directions.addAll(directionHelperService.assignCustomDirections(caseData.getAllPartiesCustom(), ALL_PARTIES));

        directions.addAll(directionHelperService.assignCustomDirections(caseData.getLocalAuthorityDirectionsCustom(),
            LOCAL_AUTHORITY));

        directions.addAll(orderByParentsAndRespondentAssignee(directionHelperService.assignCustomDirections(
            caseData.getRespondentDirectionsCustom(), PARENTS_AND_RESPONDENTS)));

        directions.addAll(directionHelperService.assignCustomDirections(caseData.getCafcassDirectionsCustom(),
            CAFCASS));

        directions.addAll(orderByOtherPartiesAssignee(directionHelperService.assignCustomDirections(
            caseData.getOtherPartiesDirectionsCustom(), OTHERS)));

        directions.addAll(directionHelperService.assignCustomDirections(caseData.getCourtDirectionsCustom(), COURT));

        return directions;
    }

    private List<Element<Direction>> orderByParentsAndRespondentAssignee(List<Element<Direction>> directions) {
        directions.sort(Comparator.comparingInt(direction -> direction.getValue()
            .getParentsAndRespondentsAssignee()
            .ordinal()));

        return directions;
    }

    private List<Element<Direction>> orderByOtherPartiesAssignee(List<Element<Direction>> directions) {
        directions.sort(Comparator.comparingInt(direction -> direction.getValue()
            .getOtherPartiesAssignee()
            .ordinal()));

        return directions;
    }

    private String formatLocalDateToMediumStyle(LocalDate date) {
        return dateFormatterService.formatLocalDateToString(date, FormatStyle.MEDIUM);
    }
}
