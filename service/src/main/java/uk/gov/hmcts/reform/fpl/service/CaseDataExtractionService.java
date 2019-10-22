package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.configuration.Display;
import uk.gov.hmcts.reform.fpl.model.configuration.OrderDefinition;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

// Supports SDO case data. Tech debt ticket needed to refactor caseDataExtractionService and NoticeOfProceedingsService
@Service
public class CaseDataExtractionService {

    private final DateFormatterService dateFormatterService;
    private final HearingBookingService hearingBookingService;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final OrdersLookupService ordersLookupService;
    private final DirectionHelperService directionHelperService;

    private static final String EMPTY_STATE_PLACEHOLDER = "BLANK - please complete";

    @Autowired
    public CaseDataExtractionService(DateFormatterService dateFormatterService,
                                     HearingBookingService hearingBookingService,
                                     HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration,
                                     OrdersLookupService ordersLookupService,
                                     DirectionHelperService directionHelperService) {
        this.dateFormatterService = dateFormatterService;
        this.hearingBookingService = hearingBookingService;
        this.hmctsCourtLookupConfiguration = hmctsCourtLookupConfiguration;
        this.ordersLookupService = ordersLookupService;
        this.directionHelperService = directionHelperService;
    }

    // TODO
    // No need to pass in CaseData to each method. Refactor to only use required model
    public Map<String, Object> getStandardOrderDirectionData(CaseData caseData) throws IOException {
        Map<String, Object> extractedHearingBookingData = getHearingBookingData(caseData);

        List<Map<String, String>> respondentsNameAndRelationship = getRespondentsNameAndRelationship(caseData);

        return ImmutableMap.<String, Object>builder()
            .put("courtName", caseData.getCaseLocalAuthority() != null
                ? hmctsCourtLookupConfiguration.getCourt(caseData.getCaseLocalAuthority()).getName()
                : EMPTY_STATE_PLACEHOLDER)
            .put("familyManCaseNumber", defaultIfNull(caseData.getFamilyManCaseNumber(), EMPTY_STATE_PLACEHOLDER))
            .put("generationDate", dateFormatterService.formatLocalDateToString(LocalDate.now(), FormatStyle.LONG))
            .put("complianceDeadline", caseData.getDateSubmitted() != null
                ? dateFormatterService.formatLocalDateToString(caseData.getDateSubmitted().plusWeeks(26),
                FormatStyle.LONG) : EMPTY_STATE_PLACEHOLDER)
            .put("children", getChildrenDetails(caseData))
            .put("respondents", respondentsNameAndRelationship)
            .put("respondentsProvided", !respondentsNameAndRelationship.isEmpty())
            .put("applicantName", getFirstApplicantName(caseData))
            .putAll(getGroupedDirections(caseData))
            .putAll(extractedHearingBookingData)
            .build();
    }

    private Map<String, Object> getHearingBookingData(CaseData caseData) {
        if (caseData.getHearingDetails() == null || caseData.getHearingDetails().isEmpty()) {
            return ImmutableMap.of(
                "hearingDate", EMPTY_STATE_PLACEHOLDER,
                "hearingVenue", EMPTY_STATE_PLACEHOLDER,
                "preHearingAttendance", EMPTY_STATE_PLACEHOLDER,
                "hearingTime", EMPTY_STATE_PLACEHOLDER,
                "judgeName", EMPTY_STATE_PLACEHOLDER
            );
        }

        HearingBooking prioritisedHearingBooking = hearingBookingService.getMostUrgentHearingBooking(caseData
            .getHearingDetails());

        return ImmutableMap.of(
            "hearingDate", dateFormatterService.formatLocalDateToString(prioritisedHearingBooking.getDate(),
                FormatStyle.LONG),
            "hearingVenue", prioritisedHearingBooking.getVenue(),
            "preHearingAttendance", prioritisedHearingBooking.getPreHearingAttendance(),
            "hearingTime", prioritisedHearingBooking.getTime(),
            "judgeName", prioritisedHearingBooking.getJudgeTitle() + " "
                + prioritisedHearingBooking.getJudgeName()
        );
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

        Map<String, List<Element<Direction>>> groupedDirections = directionHelperService.sortDirectionsByAssignee(
            directionHelperService.numberDirections(caseData.getStandardDirectionOrder().getDirections()));

        ImmutableMap.Builder<String, List<Map<String, String>>> formattedDirections = ImmutableMap.builder();

        groupedDirections.forEach((key, value) -> {
            List<Map<String, String>> directionsList = value.stream()
                .map(Element::getValue)
                .filter(direction -> !"No".equals(direction.getDirectionNeeded()))
                .map(direction -> ImmutableMap.of(
                    "title", formatTitle(direction, standardDirectionOrder.getDirections()),
                    "body", defaultIfNull(direction.getDirectionText(), EMPTY_STATE_PLACEHOLDER)))
                .collect(toList());

            formattedDirections.put(key, directionsList);
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
                    ? EMPTY_STATE_PLACEHOLDER : defaultIfNull(respondent.getFirstName(), "") + " "
                    + defaultIfNull(respondent.getLastName(), ""),
                "relationshipToChild", defaultIfNull(respondent.getRelationshipToChild(), EMPTY_STATE_PLACEHOLDER)))
            .collect(toList());
    }

    private List<Map<String, String>> getChildrenDetails(CaseData caseData) {
        // children is validated as not null
        return caseData.getAllChildren().stream()
            .map(Element::getValue)
            .map(Child::getParty)
            .map(child -> ImmutableMap.of(
                "name", child.getFirstName() + " " + child.getLastName(),
                "gender", defaultIfNull(child.getGender(), EMPTY_STATE_PLACEHOLDER),
                "dateOfBirth", child.getDateOfBirth() == null ? EMPTY_STATE_PLACEHOLDER :
                    dateFormatterService.formatLocalDateToString(child.getDateOfBirth(), FormatStyle.LONG)))
            .collect(toList());
    }

    private String getAllChildrenNames(CaseData caseData) {
        return caseData.getAllChildren().stream()
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .map(Child::getParty)
            .filter(Objects::nonNull)
            .map(childParty -> (childParty.getFirstName()) + " " + (childParty.getLastName()))
            .collect(Collectors.joining(", "));

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
