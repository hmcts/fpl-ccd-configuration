package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.configuration.Display;
import uk.gov.hmcts.reform.fpl.model.configuration.OrderDefinition;
import uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.defaultIfBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;

// Supports SDO case data. Tech debt ticket needed to refactor caseDataExtractionService and NoticeOfProceedingsService
@Slf4j
@Service
public class CaseDataExtractionService {

    private static final String EMPTY_PLACEHOLDER = "BLANK - please complete";
    private final DateFormatterService dateFormatterService;
    private final HearingBookingService hearingBookingService;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final OrdersLookupService ordersLookupService;
    private final DirectionHelperService directionHelperService;
    private final HearingVenueLookUpService hearingVenueLookUpService;
    private final CommonCaseDataExtractionService commonCaseDataExtractionService;

    @Autowired
    public CaseDataExtractionService(DateFormatterService dateFormatterService,
                                     HearingBookingService hearingBookingService,
                                     HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration,
                                     OrdersLookupService ordersLookupService,
                                     DirectionHelperService directionHelperService,
                                     HearingVenueLookUpService hearingVenueLookUpService,
                                     CommonCaseDataExtractionService commonCaseDataExtractionService) {
        this.dateFormatterService = dateFormatterService;
        this.hearingBookingService = hearingBookingService;
        this.hmctsCourtLookupConfiguration = hmctsCourtLookupConfiguration;
        this.ordersLookupService = ordersLookupService;
        this.directionHelperService = directionHelperService;
        this.hearingVenueLookUpService = hearingVenueLookUpService;
        this.commonCaseDataExtractionService = commonCaseDataExtractionService;
    }

    // TODO
    // No need to pass in CaseData to each method. Refactor to only use required model
    @SuppressWarnings("unchecked")
    public Map<String, Object> getStandardOrderDirectionData(CaseData caseData) throws IOException {
        ImmutableMap.Builder data = ImmutableMap.<String, Object>builder();

        data.put("judgeTitleAndName", JudgeAndLegalAdvisorHelper.formatJudgeTitleAndNameForDraftSDO(
            caseData.getJudgeAndLegalAdvisor(), EMPTY_PLACEHOLDER));
        data.put("legalAdvisorName", JudgeAndLegalAdvisorHelper.getLegalAdvisorName(
            caseData.getJudgeAndLegalAdvisor()));

        data.put("courtName", caseData.getCaseLocalAuthority() != null
            ? hmctsCourtLookupConfiguration.getCourt(caseData.getCaseLocalAuthority()).getName() : EMPTY_PLACEHOLDER);

        data.put("familyManCaseNumber", defaultIfNull(caseData.getFamilyManCaseNumber(), EMPTY_PLACEHOLDER));
        data.put("generationDate", dateFormatterService.formatLocalDateToString(LocalDate.now(), FormatStyle.LONG));
        data.put("complianceDeadline", caseData.getDateSubmitted() != null
            ? dateFormatterService.formatLocalDateToString(caseData.getDateSubmitted().plusWeeks(26),
            FormatStyle.LONG) : EMPTY_PLACEHOLDER);
        data.put("children", getChildrenDetails(caseData));

        List<Map<String, String>> respondentsNameAndRelationship = getRespondentsNameAndRelationship(caseData);
        data.put("respondents", respondentsNameAndRelationship);
        data.put("respondentsProvided", !respondentsNameAndRelationship.isEmpty());

        data.put("applicantName", getFirstApplicantName(caseData));
        data.putAll(getGroupedDirections(caseData));
        data.putAll(getHearingBookingData(caseData));

        if (isNotEmpty(caseData.getStandardDirectionOrder())
            && caseData.getStandardDirectionOrder().getOrderStatus() != SEALED) {
            byte[] fileContent;
            try {
                InputStream is = getClass().getResourceAsStream("/assets/images/draft-watermark.png");
                fileContent = is.readAllBytes();
                String encodedString = Base64.getEncoder().encodeToString(fileContent);

                data.put("draftbackground", String.format("image:base64:%1$s", encodedString));
            } catch (IOException e) {
                log.warn(e.getMessage());
            }
        }

        return data.build();
    }

    private Map<String, Object> getHearingBookingData(CaseData caseData) {
        if (caseData.getHearingDetails() == null || caseData.getHearingDetails().isEmpty()) {
            return ImmutableMap.of(
                "hearingDate", EMPTY_PLACEHOLDER,
                "hearingVenue", EMPTY_PLACEHOLDER,
                "preHearingAttendance", EMPTY_PLACEHOLDER,
                "hearingTime", EMPTY_PLACEHOLDER,
                "hearingJudgeTitleAndName", EMPTY_PLACEHOLDER
            );
        }

        HearingBooking prioritisedHearingBooking = hearingBookingService.getMostUrgentHearingBooking(caseData
            .getHearingDetails());

        HearingVenue hearingVenue = hearingVenueLookUpService.getHearingVenue(prioritisedHearingBooking.getVenue());

        return ImmutableMap.<String, Object>builder()
            .put("hearingDate", commonCaseDataExtractionService.getHearingDateIfHearingsOnSameDay(
                prioritisedHearingBooking)
                .orElse(""))
            .put("hearingVenue", hearingVenueLookUpService.buildHearingVenue(hearingVenue))
            .put("preHearingAttendance", commonCaseDataExtractionService.extractPrehearingAttendance(
                prioritisedHearingBooking))
            .put("hearingTime", commonCaseDataExtractionService.getHearingTime(prioritisedHearingBooking))
            .put("hearingJudgeTitleAndName", JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName(
                prioritisedHearingBooking.getJudgeAndLegalAdvisor()))
            .put("hearingLegalAdvisorName", JudgeAndLegalAdvisorHelper.getLegalAdvisorName(
                prioritisedHearingBooking.getJudgeAndLegalAdvisor()))
            .build();
    }

    private String getOrderTypes(CaseData caseData) {
        return caseData.getOrders().getOrderType().stream()
            .map(OrderType::getLabel)
            .collect(Collectors.joining(", "));
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

    private Map<String, List<Map<String, String>>> getGroupedDirections(CaseData caseData) throws IOException {
        OrderDefinition standardDirectionOrder = ordersLookupService.getStandardDirectionOrder();

        if (caseData.getStandardDirectionOrder() == null) {
            return ImmutableMap.of();
        }

        Map<DirectionAssignee, List<Element<Direction>>> groupedDirections =
            directionHelperService.sortDirectionsByAssignee(directionHelperService.numberDirections(
                caseData.getStandardDirectionOrder().getDirections()));

        ImmutableMap.Builder<String, List<Map<String, String>>> formattedDirections = ImmutableMap.builder();

        groupedDirections.forEach((key, value) -> {
            List<Map<String, String>> directionsList = value.stream()
                .map(Element::getValue)
                .filter(direction -> !"No".equals(direction.getDirectionNeeded()))
                .map(direction -> ImmutableMap.of(
                    "title", formatTitle(direction, standardDirectionOrder.getDirections()),
                    "body", defaultIfNull(direction.getDirectionText(), EMPTY_PLACEHOLDER)))
                .collect(toList());

            formattedDirections.put(key.getValue(), directionsList);
        });

        return formattedDirections.build();
    }

    private List<Map<String, String>> getRespondentsNameAndRelationship(CaseData caseData) {

        if (caseData.getRespondents1() == null || caseData.getRespondents1().isEmpty()) {
            return ImmutableList.of();
        }

        return caseData.getRespondents1().stream()
            .map(Element::getValue)
            .map(Respondent::getParty)
            .map(respondent -> ImmutableMap.of(
                "name", respondent.getFirstName() == null && respondent.getLastName() == null
                    ? EMPTY_PLACEHOLDER : defaultIfNull(respondent.getFirstName(), "") + " "
                    + defaultIfNull(respondent.getLastName(), ""),
                "relationshipToChild", defaultIfNull(respondent.getRelationshipToChild(), EMPTY_PLACEHOLDER)))
            .collect(toList());
    }

    private List<Map<String, String>> getChildrenDetails(CaseData caseData) {
        // children is validated as not null
        return caseData.getAllChildren().stream()
            .map(Element::getValue)
            .map(Child::getParty)
            .map(child -> ImmutableMap.of(
                "name", child.getFirstName() + " " + child.getLastName(),
                "gender", defaultIfNull(child.getGender(), EMPTY_PLACEHOLDER),
                "dateOfBirth", child.getDateOfBirth() == null ? EMPTY_PLACEHOLDER :
                    dateFormatterService.formatLocalDateToString(child.getDateOfBirth(), FormatStyle.LONG)))
            .collect(toList());
    }

    private String formatTitle(Direction direction, List<DirectionConfiguration> directions) {
        @AllArgsConstructor
        @NoArgsConstructor
        @Data
        class DateFormattingConfig {
            private String pattern = "h:mma, d MMMM yyyy";
            private Display.Due due = Display.Due.BY;
        }

        DateFormattingConfig dateFormattingConfig = directions.stream()
            .filter(directionConfiguration ->
                directionConfiguration.getTitle().equals(direction.getDirectionType().substring(3)))
            .map(DirectionConfiguration::getDisplay)
            .map(display -> new DateFormattingConfig(display.getTemplateDateFormat(), display.getDue()))
            .findAny()
            .orElseGet(DateFormattingConfig::new);

        return String.format(
            "%s %s %s", direction.getDirectionType(), dateFormattingConfig.due.toString().toLowerCase(),
            (direction.getDateToBeCompletedBy() != null ? dateFormatterService
                .formatLocalDateTimeBaseUsingFormat(direction.getDateToBeCompletedBy(),
                    dateFormattingConfig.getPattern()) : "unknown"));
    }
}
