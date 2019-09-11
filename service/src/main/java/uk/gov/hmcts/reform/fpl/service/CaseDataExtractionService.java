package uk.gov.hmcts.reform.fpl.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    @Autowired
    public CaseDataExtractionService(DateFormatterService dateFormatterService,
                                     HearingBookingService hearingBookingService) {
        this.dateFormatterService = dateFormatterService;
        this.hearingBookingService = hearingBookingService;
    }

    public Map<String, String> getNoticeOfProceedingTemplateData(CaseData caseData, String jurisdiction) {
        HearingBooking hearingBooking = hearingBookingService.getMostUrgentHearingBooking(caseData);

        return Map.of(
            "jurisdiction", StringUtils.defaultIfBlank(jurisdiction, ""),
            "familyManCaseNumber", StringUtils.defaultIfBlank(caseData.getFamilyManCaseNumber(), ""),
            "todaysDate", dateFormatterService.formatLocalDateToString(LocalDate.now(), FormatStyle.LONG),
            "applicantName", getFirstApplicantName(caseData),
            "orderTypes", getOrderTypes(caseData),
            "childrenNames", getAllChildrenNames(caseData),
            "hearingDate", hearingBooking.getDate() != null ? dateFormatterService
                .formatLocalDateToString(hearingBooking.getDate(), FormatStyle.LONG) : "",
            "hearingVenue", StringUtils.defaultIfBlank(hearingBooking.getVenue(), ""),
            "preHearingAttendance", StringUtils.defaultIfBlank(hearingBooking.getPreHearingAttendance(),
                ""),
            "hearingTime", StringUtils.defaultIfBlank(hearingBooking.getTime(), "")
        );
    }

    private String getOrderTypes(CaseData caseData) {
        if (caseData.getOrders() == null || caseData.getOrders().getOrderType() == null) {
            return "";
        } else {
            return caseData.getOrders().getOrderType().stream()
                .map(orderType -> orderType.getLabel())
                .collect(Collectors.joining(", "));
        }
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
}
