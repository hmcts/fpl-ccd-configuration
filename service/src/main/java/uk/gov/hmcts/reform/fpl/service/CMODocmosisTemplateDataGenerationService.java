package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.interfaces.Assignee;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.Solicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.Recital;
import uk.gov.hmcts.reform.fpl.model.common.Schedule;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.interfaces.Representable;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.RECITALS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService.DEFAULT;
import static uk.gov.hmcts.reform.fpl.service.DateFormatterService.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.service.DateFormatterService.formatLocalDateToString;

//TODO: had to extract old methods from case data extraction service to keep this from breaking.
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CMODocmosisTemplateDataGenerationService extends DocmosisTemplateDataGeneration {
    private static final String REPRESENTED_BY = "representedBy";
    private static final String NAME = "name";
    private static final String REPRESENTATIVE_NAME = "representativeName";
    private static final String REPRESENTATIVE_EMAIL = "representativeEmail";
    private static final String REPRESENTATIVE_PHONE_NUMBER = "representativePhoneNumber";
    private final CommonCaseDataExtractionService commonCaseDataExtractionService;
    private final CommonDirectionService commonDirectionService;
    private final DraftCMOService draftCMOService;
    private final HearingBookingService hearingBookingService;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final ObjectMapper mapper;

    public Map<String, Object> getTemplateData(CaseData caseData, boolean draft) throws IOException {
        Map<String, Object> cmoTemplateData = new HashMap<>();

        final DynamicList hearingDateList = caseData.getCmoHearingDateList();
        final String localAuthorityCode = caseData.getCaseLocalAuthority();

        cmoTemplateData.put("familyManCaseNumber", defaultIfNull(caseData.getFamilyManCaseNumber(), DEFAULT));
        cmoTemplateData.put("generationDate",
            formatLocalDateToString(LocalDate.now(), FormatStyle.LONG));
        cmoTemplateData.put("complianceDeadline", caseData.getDateSubmitted() != null
            ? formatLocalDateToString(caseData.getDateSubmitted().plusWeeks(26),
            FormatStyle.LONG) : DEFAULT);

        final List<Map<String, String>> childrenInCase = getChildrenDetails(caseData);
        cmoTemplateData.put("children", childrenInCase);
        cmoTemplateData.put("numberOfChildren", childrenInCase.size());

        cmoTemplateData.put("courtName", getCourtName(localAuthorityCode));

        final String applicantName = getFirstApplicantName(caseData);
        cmoTemplateData.put("applicantName", applicantName);

        cmoTemplateData.put("respondents", getRespondentsNameAndRelationship(caseData));

        cmoTemplateData.put("representatives",
            getRepresentatives(caseData, applicantName, caseData.getSolicitor()));

        CaseManagementOrder order = draftCMOService.prepareCMO(caseData, getCaseManagementOrder(caseData));

        HearingBooking nextHearing = null;

        if (order.getNextHearing() != null && order.getNextHearing().getId() != null && !order.isDraft()) {
            List<Element<HearingBooking>> hearingBookings = caseData.getHearingDetails();
            UUID nextHearingId = order.getNextHearing().getId();
            nextHearing = hearingBookingService.getHearingBookingByUUID(hearingBookings, nextHearingId);
        }

        cmoTemplateData.putAll(commonCaseDataExtractionService.getHearingBookingData(nextHearing));

        HearingBooking hearingBooking = hearingBookingService.getHearingBooking(
            caseData.getHearingDetails(), hearingDateList);
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = hearingBooking.getJudgeAndLegalAdvisor();
        cmoTemplateData.putAll(commonCaseDataExtractionService.getJudgeAndLegalAdvisorData(judgeAndLegalAdvisor));

        cmoTemplateData.putAll(getGroupedCMODirections(order));

        if (draft) {
            cmoTemplateData.putAll(getDraftWaterMarkData());
        }

        if(!draft){
            cmoTemplateData.putAll(getCourtSealData());
        }

        List<Map<String, String>> recitals = buildRecitals(order.getRecitals());
        cmoTemplateData.put(RECITALS.getKey(), recitals);
        cmoTemplateData.put("recitalsProvided", isNotEmpty(recitals));

        cmoTemplateData.putAll(getSchedule(order));

        cmoTemplateData.put("caseManagementNumber", caseData.getServedCaseManagementOrders().size() + 1);

        return cmoTemplateData;
    }

    private List<Map<String, String>> getChildrenDetails(CaseData caseData) {
        // children is validated as not null
        return caseData.getAllChildren().stream()
            .map(Element::getValue)
            .map(Child::getParty)
            .map(child -> ImmutableMap.of(
                "name", child.getFullName(),
                "gender", defaultIfNull(child.getGender(), DEFAULT),
                "dateOfBirth", child.getDateOfBirth() == null ? DEFAULT :
                    formatLocalDateToString(child.getDateOfBirth(), FormatStyle.LONG)))
            .collect(toList());
    }

    private String getFirstApplicantName(CaseData caseData) {
        return caseData.getAllApplicants().stream()
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .map(Applicant::getParty)
            .filter(Objects::nonNull)
            .map(ApplicantParty::getOrganisationName)
            .findFirst()
            .orElse("");
    }

    private List<Map<String, String>> getRespondentsNameAndRelationship(CaseData caseData) {
        if (isEmpty(caseData.getRespondents1())) {
            return emptyList();
        }

        return caseData.getRespondents1().stream()
            .map(Element::getValue)
            .map(Respondent::getParty)
            .map(respondent -> ImmutableMap.of(
                "name", respondent.getFullName(),
                "relationshipToChild", defaultIfNull(respondent.getRelationshipToChild(), DEFAULT)))
            .collect(toList());
    }

    private CaseManagementOrder getCaseManagementOrder(CaseData caseData) {
        if (caseData.getCaseManagementOrder() != null) {
            return caseData.getCaseManagementOrder();
        }

        return null;
    }

    private List<Map<String, Object>> getRepresentatives(CaseData caseData,
                                                         String applicantName,
                                                         Solicitor solicitor) {

        List<Map<String, Object>> representativesInfo = new ArrayList<>();
        List<Element<Representative>> representatives = caseData.getRepresentatives();

        representativesInfo.add(getApplicantDetails(applicantName, solicitor));

        ElementUtils.unwrapElements(caseData.getRespondents1()).stream()
            .filter(respondent -> isNotEmpty(respondent.getRepresentedBy()))
            .forEach(respondent -> representativesInfo.add(Map.of(
                NAME, defaultIfNull(respondent.getParty().getFullName(), EMPTY),
                REPRESENTED_BY, getRepresentativesInfo(respondent, representatives))
            ));


        caseData.getAllOthers().stream()
            .map(Element::getValue)
            .filter(other -> isNotEmpty(other.getRepresentedBy()))
            .forEach(other -> representativesInfo.add(Map.of(
                NAME, defaultIfNull(other.getName(), EMPTY),
                REPRESENTED_BY, getRepresentativesInfo(other, representatives))));

        return representativesInfo;
    }

    private List<Map<String, Object>> getRepresentativesInfo(Representable representable,
                                                             List<Element<Representative>> representatives) {
        return representable.getRepresentedBy().stream()
            .map(representativeId -> findRepresentative(representatives, representativeId.getValue()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(this::buildRepresentativeInfo)
            .collect(Collectors.toList());
    }

    private Optional<Representative> findRepresentative(List<Element<Representative>> representatives, UUID id) {
        return representatives.stream()
            .filter(representative -> representative.getId().equals(id))
            .findFirst()
            .map(Element::getValue);
    }

    private Map<String, Object> buildRepresentativeInfo(Representative representative) {
        return ImmutableMap.of(
            REPRESENTATIVE_NAME, representative.getFullName(),
            REPRESENTATIVE_EMAIL, defaultIfNull(representative.getEmail(), EMPTY),
            REPRESENTATIVE_PHONE_NUMBER, defaultIfNull(representative.getTelephoneNumber(), EMPTY)
        );
    }

    private Map<String, Object> getSchedule(CaseManagementOrder caseManagementOrder) {
        final Schedule schedule = caseManagementOrder.getSchedule();
        Map<String, Object> scheduleMap = new LinkedHashMap<>();

        if (isScheduleIncluded(schedule)) {
            scheduleMap.putAll(getEmptyScheduleMap());
        } else {
            scheduleMap.putAll(mapper.convertValue(schedule, new TypeReference<>() {
            }));
            scheduleMap.put("scheduleProvided", true);
        }

        return scheduleMap;
    }

    private boolean isScheduleIncluded(Schedule schedule) {
        return (schedule == null) || schedule.getIncludeSchedule().equals("No");
    }

    private Map<String, Object> getEmptyScheduleMap() {
        final String[] scheduleKeys = {
            "includeSchedule", "allocation", "application", "todaysHearing", "childrensCurrentArrangement",
            "timetableForProceedings", "timetableForChildren", "alternativeCarers", "threshold", "keyIssues",
            "partiesPositions"
        };

        Map<String, Object> scheduleMap = new LinkedHashMap<>();

        Arrays.stream(scheduleKeys).forEach(key -> scheduleMap.put(key, DEFAULT));

        scheduleMap.put("scheduleProvided", false);

        return scheduleMap;
    }

    private Map<String, Object> getApplicantDetails(String applicantName, Solicitor solicitor) {
        Map<String, Object> applicantDetails = new HashMap<>();

        applicantDetails.put("name", defaultIfBlank(applicantName, DEFAULT));

        if (solicitor == null) {
            applicantDetails.put(REPRESENTED_BY, List.of(Map.of(
                REPRESENTATIVE_NAME, DEFAULT,
                REPRESENTATIVE_EMAIL, DEFAULT,
                REPRESENTATIVE_PHONE_NUMBER, DEFAULT
            )));
        } else {
            String phoneNumber = defaultIfBlank(solicitor.getTelephone(), solicitor.getMobile());
            applicantDetails.put(REPRESENTED_BY, List.of(
                Map.of(
                    REPRESENTATIVE_NAME, defaultIfBlank(solicitor.getName(), DEFAULT),
                    REPRESENTATIVE_EMAIL, defaultIfBlank(solicitor.getEmail(), DEFAULT),
                    REPRESENTATIVE_PHONE_NUMBER, defaultIfBlank(phoneNumber, DEFAULT)
                )));
        }

        return applicantDetails;

    }

    private String getCourtName(String localAuthorityCode) {
        if (isBlank(localAuthorityCode)) {
            return DEFAULT;
        }
        return defaultIfBlank(hmctsCourtLookupConfiguration.getCourt(localAuthorityCode).getName(), DEFAULT);
    }

    private Map<String, Object> getGroupedCMODirections(final CaseManagementOrder caseManagementOrder) {
        if (caseManagementOrder == null || isEmpty(caseManagementOrder.getDirections())) {
            return ImmutableMap.of();
        }

        Map<DirectionAssignee, List<Element<Direction>>> directions =
            commonDirectionService.sortDirectionsByAssignee(commonDirectionService.numberDirections(
                caseManagementOrder.getDirections()));

        List<Element<Direction>> respondents = defaultIfNull(directions.remove(PARENTS_AND_RESPONDENTS), emptyList());
        List<Element<Direction>> otherParties = defaultIfNull(directions.remove(OTHERS), emptyList());
        ImmutableMap.Builder<String, Object> formattedDirections = ImmutableMap.builder();

        final Map<Assignee, List<Element<Direction>>> respondentDirections =
            respondents.stream()
                .collect(groupingBy(element -> element.getValue().getParentsAndRespondentsAssignee()));

        final Map<Assignee, List<Element<Direction>>> otherPartyDirections = otherParties.stream()
            .collect(groupingBy(element -> element.getValue().getOtherPartiesAssignee()));

        formattedDirections.put(PARENTS_AND_RESPONDENTS.getValue(),
            getFormattedDirections(respondentDirections));

        formattedDirections.put(OTHERS.getValue(), getFormattedDirections(otherPartyDirections));

        directions.forEach((key, value) -> {
            List<Map<String, String>> directionsList = buildFormattedDirectionList(value);
            formattedDirections.put(key.getValue(), directionsList);
        });

        return formattedDirections.build();
    }

    private List<Map<String, Object>> getFormattedDirections(
        Map<Assignee, List<Element<Direction>>> groupedDirections) {

        List<Map<String, Object>> directions = new ArrayList<>();
        groupedDirections.forEach((assignee, assigneeDirection) -> {
            Map<String, Object> direction = new HashMap<>();
            direction.put("header", "For " + assignee.getLabel());
            List<Map<String, String>> directionsList = buildFormattedDirectionList(
                assigneeDirection);
            direction.put("directions", directionsList);
            directions.add(direction);
        });

        return directions;
    }

    private List<Map<String, String>> buildFormattedDirectionList(List<Element<Direction>> directions) {
        return directions.stream()
            .map(Element::getValue)
            .filter(direction -> !"No".equals(direction.getDirectionNeeded()))
            .map(direction -> ImmutableMap.of(
                "title", formatTitle(direction),
                "body", defaultIfNull(direction.getDirectionText(), DEFAULT)))
            .collect(toList());
    }

    private String formatTitle(Direction direction) {
        return String.format("%s by %s",
            direction.getDirectionType(),
            direction.getDateToBeCompletedBy() != null ? formatLocalDateTimeBaseUsingFormat(
                direction.getDateToBeCompletedBy(), "h:mma, d MMMM yyyy") : "unknown");
    }

    private List<Map<String, String>> buildRecitals(final List<Element<Recital>> recitals) {
        if (isEmpty(recitals)) {
            return emptyList();
        }

        return recitals.stream()
            .filter(Objects::nonNull)
            .map(Element::getValue)
            .map(recital -> ImmutableMap.of(
                "title", defaultString(recital.getTitle(), DEFAULT),
                "body", defaultString(recital.getDescription(), DEFAULT)
            ))
            .collect(toList());
    }
}
