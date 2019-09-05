package uk.gov.hmcts.reform.fpl.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CaseDataExtractionService {
    public Map<String, String> getNoticeOfProceedingTemplateData(CaseData caseData, String jurisdiction) {
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
        String todaysDate = DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime);

        HearingBooking hearingBooking = getMostUrgentHearingBooking(caseData);

        return Map.of(
            "jurisdiction", StringUtils.defaultIfBlank(jurisdiction, ""),
            "familyManCaseNumber", StringUtils.defaultIfBlank(caseData.getFamilyManCaseNumber(), ""),
            "todaysDate", todaysDate,
            "applicantName", getFirstApplicantName(caseData),
            "orderTypes", getOrderTypes(caseData),
            "childrenNames", getAllChildrenNames(caseData),
            "hearingDate", hearingBooking.getDate() != null ? hearingBooking.getDate().toString() : "",
            "hearingVenue", StringUtils.defaultIfBlank(hearingBooking.getVenue(), ""),
            "preHearingAttendance", StringUtils.defaultIfBlank(hearingBooking.getPreHearingAttendance(),
                ""),
            "hearingTime", StringUtils.defaultIfBlank(hearingBooking.getTime(), "")
        );
    }

    public HearingBooking getMostUrgentHearingBooking(CaseData caseData) {
        // TODO
        // Re-work this. Do not send empty HearingBooking
        if (caseData.getHearingDetails() == null) {
            return HearingBooking.builder().build();
        }

        return caseData.getHearingDetails().stream()
            .map(Element::getValue)
            .sorted((booking1, booking2) -> booking1.getDate().compareTo(booking2.getDate()))
            .findFirst()
            .orElseThrow();
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
            .map(childParty -> {
                return (childParty.getFirstName()) + " " + (childParty.getLastName());
            }).collect(Collectors.joining(", "));
    }
}
