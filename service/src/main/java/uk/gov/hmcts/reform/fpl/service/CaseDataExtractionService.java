package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Service
public class CaseDataExtractionService {

    private DateFormatterService dateFormatterService;
    private HearingBookingService hearingBookingService;
    private HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;

    @Autowired
    public CaseDataExtractionService(DateFormatterService dateFormatterService,
                                     HearingBookingService hearingBookingService,
                                     HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration) {
        this.dateFormatterService = dateFormatterService;
        this.hearingBookingService = hearingBookingService;
        this.hmctsCourtLookupConfiguration = hmctsCourtLookupConfiguration;
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
    public Map<String, Object> getDraftStandardOrderDirectionTemplateData(CaseData caseData) {
        Map<String, Object> extractedHearingBookingData = getHearingBookingData(caseData);

        return ImmutableMap.<String, Object>builder()
            .put("courtName", hmctsCourtLookupConfiguration.getCourt(caseData.getCaseLocalAuthority()).getName())
            .put("familyManCaseNumber", caseData.getFamilyManCaseNumber())
            .put("generationDate",  dateFormatterService.formatLocalDateToString(LocalDate.now(), FormatStyle.LONG))
            .put("complianceDeadline", dateFormatterService
                .formatLocalDateToString(caseData.getDateSubmitted().plusWeeks(26), FormatStyle.LONG))
            .put("children", getChildrenDetails(caseData))
            .put("directions", getStandardOrderDirections(caseData))
            .put("respondents", getRespondentsDetails(caseData))
            .putAll(extractedHearingBookingData)
            .build();
    }

    private Map<String, Object> getHearingBookingData(CaseData caseData) {
        HearingBooking prioritisedHearingBooking = hearingBookingService.getMostUrgentHearingBooking(caseData);

        return ImmutableMap.of(
            "hearingDate", dateFormatterService.formatLocalDateToString(prioritisedHearingBooking.getDate(),
                FormatStyle.LONG),
            "hearingVenue", prioritisedHearingBooking.getVenue(),
            "preHearingAttendance", prioritisedHearingBooking.getPreHearingAttendance(),
            "hearingTime", prioritisedHearingBooking.getTime()
        );
    }

    private String getOrderTypes(CaseData caseData) {
        return caseData.getOrders().getOrderType().stream()
            .map(orderType -> orderType.getLabel())
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

    // TODO
    // Respondents is not mandatory. Check with BA what we do when we do not have respondents
    private List<Map<String, String>> getRespondentsDetails(CaseData caseData) {
        return caseData.getRespondents1().stream()
            .map(Element::getValue)
            .map(respondent -> ImmutableMap.of(
                "name", respondent.getFirstName() + " " + respondent.getLastName(),
                "relationshipToChild", respondent.getRelationshipToChild()))
            .collect(toList());
    }

    private String getAllChildrenNames(CaseData caseData) {
        return getChildrenDetails(caseData).stream()
            .map(element -> element.get("name"))
            .collect(Collectors.joining(", "));
    }

    private List<Map<String, String>> getChildrenDetails(CaseData caseData) {
        return caseData.getAllChildren().stream()
            .map(Element::getValue)
            .map(Child::getParty)
            .map(child -> ImmutableMap.of(
                // TODO
                // Joining name is common theme. Move to util method and use static import
                "name", child.getFirstName() + " " + child.getLastName(),
                "gender", defaultIfNull(child.getGender(), "unknown"),
                "dateOfBirth", child.getDateOfBirth() == null ? "unknown" :
                    dateFormatterService.formatLocalDateToString(child.getDateOfBirth(), FormatStyle.LONG)))
            .collect(toList());
    }

    private List<Map<String, String>> getStandardOrderDirections(CaseData caseData) {
        return caseData.getStandardDirectionOrder().getDirections()
            .stream()
            .map(Element::getValue)
            .map(direction -> Map.of(
                "title", direction.getType() + " comply by: " + (direction.getCompleteBy() != null
                    ? formatDate(direction) : " unknown"),
                "body", direction.getText()))
            .collect(toList());
    }

    // TODO
    // Move to seperate service
    private String formatDate(Direction direction) {
        return direction.getCompleteBy().format(DateTimeFormatter.ofPattern("h:mma, d MMMM yyyy"))
            .replace("AM", "am")
            .replace("PM", "pm");
    }
}
