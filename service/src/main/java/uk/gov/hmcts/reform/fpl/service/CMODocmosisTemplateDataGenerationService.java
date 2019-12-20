package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.OtherPartiesDirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.ParentsAndRespondentsDirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Representative;
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
import static uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService.EMPTY_PLACEHOLDER;

// REFACTOR: 02/12/2019 Refactor this with CaseDataExtractionService and NotifyOfProceedingService to try and
//  extract common elements to CommonCaseDataExtractionService
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CMODocmosisTemplateDataGenerationService extends DocmosisTemplateDataGeneration {
    public static final String REPRESENTED_BY = "representedBy";
    public static final String NAME = "name";
    public static final String REPRESENTATIVE_NAME = "representativeName";
    public static final String REPRESENTATIVE_EMAIL = "representativeEmail";
    public static final String REPRESENTATIVE_PHONE_NUMBER = "representativePhoneNumber";
    private final CommonCaseDataExtractionService commonCaseDataExtractionService;
    private final CaseDataExtractionService caseDataExtractionService;
    private final DateFormatterService dateFormatterService;
    private final DirectionHelperService directionHelperService;
    private final DraftCMOService draftCMOService;
    private final HearingBookingService hearingBookingService;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final ObjectMapper mapper;

    @SuppressWarnings("unchecked")
    public Map<String, Object> getTemplateData(CaseData caseData, boolean draft) throws IOException {
        ImmutableMap.Builder cmoTemplateData = ImmutableMap.<String, Object>builder();
        final DynamicList hearingDateList = caseData.getCmoHearingDateList();
        final String localAuthorityCode = caseData.getCaseLocalAuthority();

        cmoTemplateData.put("familyManCaseNumber", defaultIfNull(caseData.getFamilyManCaseNumber(), EMPTY_PLACEHOLDER));
        cmoTemplateData.put("generationDate",
            dateFormatterService.formatLocalDateToString(LocalDate.now(), FormatStyle.LONG));
        cmoTemplateData.put("complianceDeadline", caseData.getDateSubmitted() != null
            ? dateFormatterService.formatLocalDateToString(caseData.getDateSubmitted().plusWeeks(26),
            FormatStyle.LONG) : EMPTY_PLACEHOLDER);

        final List<Map<String, String>> childrenInCase = caseDataExtractionService.getChildrenDetails(caseData);
        cmoTemplateData.put("children", childrenInCase);
        cmoTemplateData.put("numberOfChildren", childrenInCase.size());

        cmoTemplateData.put("courtName", getCourtName(localAuthorityCode));

        final String applicantName = caseDataExtractionService.getFirstApplicantName(caseData);
        cmoTemplateData.put("applicantName", applicantName);

        List<Map<String, String>> respondentsNameAndRelationship = caseDataExtractionService
            .getRespondentsNameAndRelationship(caseData);

        cmoTemplateData.put("respondents", respondentsNameAndRelationship);

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

        List<Map<String, String>> recitals = buildRecitals(order.getRecitals());
        cmoTemplateData.put(RECITALS.getKey(), recitals);
        cmoTemplateData.put("recitalsProvided", isNotEmpty(recitals));

        cmoTemplateData.putAll(getSchedule(order));

        cmoTemplateData.put("caseManagementNumber", caseData.getServedCaseManagementOrders().size() + 1);

        return cmoTemplateData.build();
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

        Arrays.stream(scheduleKeys).forEach(key -> scheduleMap.put(key, EMPTY_PLACEHOLDER));

        scheduleMap.put("scheduleProvided", false);

        return scheduleMap;
    }

    private Map<String, Object> getApplicantDetails(String applicantName, Solicitor solicitor) {
        Map<String, Object> applicantDetails = new HashMap<>();

        applicantDetails.put("name", defaultIfBlank(applicantName, EMPTY_PLACEHOLDER));

        if (solicitor == null) {
            applicantDetails.put(REPRESENTED_BY, List.of(Map.of(
                REPRESENTATIVE_NAME, EMPTY_PLACEHOLDER,
                REPRESENTATIVE_EMAIL, EMPTY_PLACEHOLDER,
                REPRESENTATIVE_PHONE_NUMBER, EMPTY_PLACEHOLDER
            )));
        } else {
            String phoneNumber = defaultIfBlank(solicitor.getTelephone(), solicitor.getMobile());
            applicantDetails.put(REPRESENTED_BY, List.of(
                Map.of(
                    REPRESENTATIVE_NAME, defaultIfBlank(solicitor.getName(), EMPTY_PLACEHOLDER),
                    REPRESENTATIVE_EMAIL, defaultIfBlank(solicitor.getEmail(), EMPTY_PLACEHOLDER),
                    REPRESENTATIVE_PHONE_NUMBER, defaultIfBlank(phoneNumber, EMPTY_PLACEHOLDER)
                )));
        }

        return applicantDetails;

    }

    private String getCourtName(String localAuthorityCode) {
        if (isBlank(localAuthorityCode)) {
            return EMPTY_PLACEHOLDER;
        }
        return defaultIfBlank(hmctsCourtLookupConfiguration.getCourt(localAuthorityCode).getName(), EMPTY_PLACEHOLDER);
    }

    private Map<String, Object> getGroupedCMODirections(final CaseManagementOrder caseManagementOrder) {
        if (caseManagementOrder == null || isEmpty(caseManagementOrder.getDirections())) {
            return ImmutableMap.of();
        }

        Map<DirectionAssignee, List<Element<Direction>>> directions =
            directionHelperService.sortDirectionsByAssignee(directionHelperService.numberDirections(
                caseManagementOrder.getDirections()));

        List<Element<Direction>> respondents = defaultIfNull(directions.remove(PARENTS_AND_RESPONDENTS), emptyList());
        List<Element<Direction>> otherParties = defaultIfNull(directions.remove(OTHERS), emptyList());
        ImmutableMap.Builder<String, Object> formattedDirections = ImmutableMap.builder();

        final Map<ParentsAndRespondentsDirectionAssignee, List<Element<Direction>>> respondentDirections =
            respondents.stream()
                .collect(groupingBy(element -> element.getValue().getParentsAndRespondentsAssignee()));

        final Map<OtherPartiesDirectionAssignee, List<Element<Direction>>> otherPartyDirections = otherParties.stream()
            .collect(groupingBy(element -> element.getValue().getOtherPartiesAssignee()));

        formattedDirections.put(PARENTS_AND_RESPONDENTS.getValue(),
            getFormattedParentsAndRespondentsDirections(respondentDirections));

        formattedDirections.put(OTHERS.getValue(), getFormattedOtherPartiesDirections(otherPartyDirections));

        directions.forEach((key, value) -> {
            List<Map<String, String>> directionsList = buildFormattedDirectionList(value);
            formattedDirections.put(key.getValue(), directionsList);
        });

        return formattedDirections.build();
    }

    private List<Map<String, Object>> getFormattedOtherPartiesDirections(
        Map<OtherPartiesDirectionAssignee, List<Element<Direction>>> groupedOtherParties) {

        List<Map<String, Object>> directionsToOthers = new ArrayList<>();
        groupedOtherParties.forEach((key, value) -> {
            Map<String, Object> directionForOthers = new HashMap<>();
            directionForOthers.put("header", "For " + key.getLabel());
            List<Map<String, String>> directionsList = buildFormattedDirectionList(
                value);
            directionForOthers.put("directions", directionsList);
            directionsToOthers.add(directionForOthers);
        });

        return directionsToOthers;
    }

    private List<Map<String, Object>> getFormattedParentsAndRespondentsDirections(
        Map<ParentsAndRespondentsDirectionAssignee, List<Element<Direction>>> groupedParentsAndRespondents) {

        List<Map<String, Object>> directionsToRespondents = new ArrayList<>();
        groupedParentsAndRespondents.forEach((key, value) -> {
            Map<String, Object> directionForRespondent = new HashMap<>();
            directionForRespondent.put("header", "For " + key.getLabel());
            List<Map<String, String>> directionsList = buildFormattedDirectionList(
                value);
            directionForRespondent.put("directions", directionsList);
            directionsToRespondents.add(directionForRespondent);
        });

        return directionsToRespondents;
    }

    private List<Map<String, String>> buildFormattedDirectionList(List<Element<Direction>> directions) {
        return directions.stream()
            .map(Element::getValue)
            .filter(direction -> !"No".equals(direction.getDirectionNeeded()))
            .map(direction -> ImmutableMap.of(
                "title", formatTitle(direction),
                "body", defaultIfNull(direction.getDirectionText(), EMPTY_PLACEHOLDER)))
            .collect(toList());
    }

    private String formatTitle(Direction direction) {
        return String.format("%s by %s",
            direction.getDirectionType(),
            direction.getDateToBeCompletedBy() != null
                ? dateFormatterService.formatLocalDateTimeBaseUsingFormat(
                direction.getDateToBeCompletedBy(), "h:mma, d MMMM yyyy")
                : "unknown");
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
}
