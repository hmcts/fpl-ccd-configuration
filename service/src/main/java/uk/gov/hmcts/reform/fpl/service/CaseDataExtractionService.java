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

@Service
public class CaseDataExtractionService {

    private final DateFormatterService dateFormatterService;
    private final HearingBookingService hearingBookingService;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final OrdersLookupService ordersLookupService;

    private static final String EMPTY_STATE_PLACEHOLDER = "BLANK - please complete";

    @Autowired
    public CaseDataExtractionService(DateFormatterService dateFormatterService,
                                     HearingBookingService hearingBookingService,
                                     HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration,
                                     OrdersLookupService ordersLookupService) {
        this.dateFormatterService = dateFormatterService;
        this.hearingBookingService = hearingBookingService;
        this.hmctsCourtLookupConfiguration = hmctsCourtLookupConfiguration;
        this.ordersLookupService = ordersLookupService;
    }

    // Validation within our frontend ensures that the following data is present
    public Map<String, Object> getNoticeOfProceedingTemplateData(CaseData caseData) {

        Map<String, Object> extractedHearingBookingData = getHearingBookingData(caseData);

        return ImmutableMap.<String, Object>builder()
            .put("courtName", hmctsCourtLookupConfiguration.getCourt(caseData.getCaseLocalAuthority()).getName())
            .put("familyManCaseNumber", caseData.getFamilyManCaseNumber())
            .put("todaysDate", dateFormatterService.formatLocalDateToString(LocalDate.now(), FormatStyle.LONG))
            .put("applicantName", getFirstApplicantName(caseData))
            .put("orderTypes", getOrderTypes(caseData))
            .put("childrenNames", getAllChildrenNames(caseData))
            .putAll(extractedHearingBookingData)
            .build();
    }

    // TODO
    // No need to pass in CaseData to each method. Refactor to only use required model
    public Map<String, Object> getDraftStandardOrderDirectionTemplateData(CaseData caseData) throws IOException {
        Map<String, Object> extractedHearingBookingData = getHearingBookingData(caseData);

        OrderDefinition standardDirectionOrder = ordersLookupService.getStandardDirectionOrder();

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
            .put("respondents", getRespondentsNameAndRelationship(caseData))
            .put("applicantName", getFirstApplicantName(caseData))
            .put("standardDirectionOrders",
                getStandardOrderDirections(caseData, standardDirectionOrder.getDirections()))
            .putAll(extractedHearingBookingData)
            .build();
    }

    private Map<String, Object> getHearingBookingData(CaseData caseData) {

        // TODO
        // Rethink how we structure hearing. c6 c6a has defined hearing as flat properties
        if (caseData.getHearingDetails() == null || caseData.getHearingDetails().isEmpty()) {
            return ImmutableMap.of(
                "hearingDate", EMPTY_STATE_PLACEHOLDER,
                "hearingVenue", EMPTY_STATE_PLACEHOLDER,
                "preHearingAttendance", EMPTY_STATE_PLACEHOLDER,
                "hearingTime", EMPTY_STATE_PLACEHOLDER,
                "judgeName", EMPTY_STATE_PLACEHOLDER
            );
        }

        HearingBooking prioritisedHearingBooking = hearingBookingService.getMostUrgentHearingBooking(caseData);

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

        if (caseData.getAllApplicants() == null || caseData.getAllApplicants().isEmpty()) {
            return EMPTY_STATE_PLACEHOLDER;
        }

        return caseData.getAllApplicants().stream()
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .map(Applicant::getParty)
            .filter(Objects::nonNull)
            .map(ApplicantParty::getOrganisationName)
            .findFirst()
            .orElse("");
    }

    // TODO
    // Respondents is not mandatory. Check with BA what we do when we do not have respondents
    private List<Map<String, String>> getRespondentsNameAndRelationship(CaseData caseData) {

        if (caseData.getRespondents1() == null || caseData.getRespondents1().isEmpty()) {
            return ImmutableList.of();
        }

        return caseData.getRespondents1().stream()
            .map(Element::getValue)
            .map(respondent -> ImmutableMap.of(
                "name", respondent.getFirstName() == null && respondent.getLastName() == null
                    ? EMPTY_STATE_PLACEHOLDER : defaultIfNull(respondent.getFirstName(), "") + " "
                    + defaultIfNull(respondent.getLastName(), ""),
                "relationshipToChild", defaultIfNull(respondent.getRelationshipToChild(), EMPTY_STATE_PLACEHOLDER)))
            .collect(toList());
    }

    private String getAllChildrenNames(CaseData caseData) {
        return getChildrenDetails(caseData).stream()
            .map(element -> element.get("name"))
            .collect(Collectors.joining(", "));
    }

    private List<Map<String, String>> getChildrenDetails(CaseData caseData) {
        // children is validated as not null
        return caseData.getAllChildren().stream()
            .map(Element::getValue)
            .map(Child::getParty)
            .map(child -> ImmutableMap.of(
                // TODO
                // Joining name is common theme. Move to util method and use static import
                "name", child.getFirstName() + " " + child.getLastName(),
                "gender", defaultIfNull(child.getGender(), EMPTY_STATE_PLACEHOLDER),
                "dateOfBirth", child.getDateOfBirth() == null ? EMPTY_STATE_PLACEHOLDER :
                    dateFormatterService.formatLocalDateToString(child.getDateOfBirth(), FormatStyle.LONG)))
            .collect(toList());
    }

    private List<Map<String, String>> getStandardOrderDirections(CaseData caseData,
                                                                 List<DirectionConfiguration> directions) {

        if (caseData.getStandardDirectionOrder() == null
            || caseData.getStandardDirectionOrder().getDirections() == null) {
            return ImmutableList.of();
        }

        //TODO: null pointer is thrown when direction.getText() is null. Hidden values added in aboutToSubmit currently
        // Have set defaultIfNull for now.
        return caseData.getStandardDirectionOrder().getDirections()
            .stream()
            .map(Element::getValue)
            .map(direction -> ImmutableMap.of(
                "title", formatTitle(direction, directions),
                "body", defaultIfNull(direction.getText(), "BLANK")))
            .collect(toList());
    }


    @SuppressWarnings("LineLength")
    private String formatTitle(Direction direction, List<DirectionConfiguration> directions) {
        @AllArgsConstructor
        @NoArgsConstructor
        @Data
        class DateFormattingConfig {
            private String pattern = "h:mma, d MMMM yyyy";
            private Display.Due due = Display.Due.BY;
        }

        DateFormattingConfig dateFormattingConfig = directions.stream()
            .filter(directionConfiguration -> directionConfiguration.getTitle().equals(direction.getType()))
            .map(DirectionConfiguration::getDisplay)
            .map(display -> new DateFormattingConfig(display.getTemplateDateFormat(), display.getDue()))
            .findAny()
            .orElseGet(DateFormattingConfig::new);

        return String.format("%s %s %s", direction.getType(), dateFormattingConfig.due.toString().toLowerCase(),
            (direction.getCompleteBy() != null ? dateFormatterService.formatLocalDateTimeBaseUsingFormat(direction.getCompleteBy(),
                dateFormattingConfig.getPattern()) : "unknown"));
    }
}
