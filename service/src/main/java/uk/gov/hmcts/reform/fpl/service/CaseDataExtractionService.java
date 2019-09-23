package uk.gov.hmcts.reform.fpl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

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

    public Map<String, String> getNoticeOfProceedingTemplateData(CaseData caseData) {
        // Validation within our frontend ensures that the following data is present
        Map<String, String> extractedHearingBookingData = getPrioritisedHearingBookingData(caseData);

        Map<String, String> templateData =  Map.of(
            "courtName", hmctsCourtLookupConfiguration.getCourt(caseData.getCaseLocalAuthority()).getName(),
            "familyManCaseNumber", caseData.getFamilyManCaseNumber(),
            "todaysDate", dateFormatterService.formatLocalDateToString(LocalDate.now(), FormatStyle.LONG),
            "applicantName", getFirstApplicantName(caseData),
            "orderTypes", getOrderTypes(caseData),
            "childrenNames", getAllChildrenNames(caseData)
        );

        templateData.putAll(extractedHearingBookingData);

        return templateData;
    }

    public Map<String, Object> getDraftStandardOrderDirectionTemplateData(CaseData caseData) {
        Map<String, String> extractedHearingBookingData = getPrioritisedHearingBookingData(caseData);

        Map<String, Object> templateData = Map.of(
            "familyManCaseId", caseData.getFamilyManCaseNumber(),
            "generationDateStr",  dateFormatterService.formatLocalDateToString(LocalDate.now(), FormatStyle.LONG),
            "complianceDeadline", dateFormatterService.formatLocalDateToString(caseData.getDateSubmitted().plusWeeks(26), FormatStyle.LONG),
            "children", prepareChildrenDetails(caseData),
            "directions", prepareDirections(caseData)
        );

        templateData.putAll(extractedHearingBookingData);

        return templateData;
    }

    private Map<String, String> getPrioritisedHearingBookingData(CaseData caseData) {
        HearingBooking prioritisedHearingBooking = hearingBookingService.getMostUrgentHearingBooking(caseData);
        return getHearingBookingData(prioritisedHearingBooking);
    }

    private Map<String, String> getHearingBookingData(HearingBooking hearingBooking) {
        return Map.of(
            "hearingDate", dateFormatterService.formatLocalDateToString(hearingBooking.getDate(), FormatStyle.LONG),
            "hearingVenue", hearingBooking.getVenue(),
            "preHearingAttendance", hearingBooking.getPreHearingAttendance(),
            "hearingTime", hearingBooking.getTime()
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

    private String getAllChildrenNames(CaseData caseData) {
        return caseData.getAllChildren().stream()
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .map(Child::getParty)
            .filter(Objects::nonNull)
            .map(childParty -> (childParty.getFirstName()) + " " + (childParty.getLastName()))
                .collect(Collectors.joining(", "));
    }

    private List<Map<String, String>> prepareChildrenDetails(CaseData caseData) {
        return caseData.getAllChildren()
            .stream()
            .map(Element::getValue)
            .map(Child::getParty)
            .map(child -> Map.of(
                "name", child.getFirstName() + " " + child.getLastName(),
                "gender", defaultIfEmpty(child.getGender(), "unknown"),
                "dateOfBirth", child.getDateOfBirth().toString()))
            .collect(toList());
    }

    private List<Map<String, String>> prepareDirections(CaseData caseData) {
        return caseData.getStandardDirectionOrder().getDirections()
            .stream()
            .map(Element::getValue)
            .map(direction -> Map.of(
                "title", direction.getType() + " comply by: " +
                    (direction.getCompleteBy() != null ? direction.getCompleteBy() : " unknown"),
                "body", direction.getText()))
            .collect(toList());
    }
}
