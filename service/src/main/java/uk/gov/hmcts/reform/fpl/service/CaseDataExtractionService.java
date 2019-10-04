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
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
        HearingBooking hearingBooking = hearingBookingService.getMostUrgentHearingBooking(caseData);

        // Validation within our frontend ensures that the following data is present
        return Map.of(
            "courtName", hmctsCourtLookupConfiguration.getCourt(caseData.getCaseLocalAuthority()).getName(),
            "familyManCaseNumber", caseData.getFamilyManCaseNumber(),
            "todaysDate", dateFormatterService.formatLocalDateToString(LocalDate.now(), FormatStyle.LONG),
            "applicantName", getFirstApplicantName(caseData),
            "orderTypes", getOrderTypes(caseData),
            "childrenNames", getAllChildrenNames(caseData),
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
        String childrenNames = caseData.getAllChildren().stream()
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .map(Child::getParty)
            .filter(Objects::nonNull)
            .map(childParty -> (childParty.getFirstName()) + " " + (childParty.getLastName()))
            .collect(Collectors.joining(", "));

        StringBuilder stringBuilder = new StringBuilder(childrenNames);
        stringBuilder.replace(childrenNames.lastIndexOf(","), childrenNames.lastIndexOf(",") + 1, "and");
        childrenNames = stringBuilder.toString();

        return childrenNames;
    }
}
