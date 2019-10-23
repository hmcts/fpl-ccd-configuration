package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;

@Service
public class NoticeOfProceedingService {
    private DateFormatterService dateFormatterService;
    private HearingBookingService hearingBookingService;
    private HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;

    @Autowired
    public NoticeOfProceedingService(DateFormatterService dateFormatterService,
                                     HearingBookingService hearingBookingService,
                                     HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration) {
        this.dateFormatterService = dateFormatterService;
        this.hearingBookingService = hearingBookingService;
        this.hmctsCourtLookupConfiguration = hmctsCourtLookupConfiguration;
    }

    public Map<String, Object> getNoticeOfProceedingTemplateData(CaseData caseData) {
        Map<String, String> hearingBookingData = getHearingBookingData(caseData.getHearingDetails());

        // Validation within our frontend ensures that the following data is present
        return ImmutableMap.<String, Object>builder()
            .put("courtName", getCourtName(caseData.getCaseLocalAuthority()))
            .put("familyManCaseNumber", caseData.getFamilyManCaseNumber())
            .put("todaysDate", dateFormatterService.formatLocalDateToString(LocalDate.now(), FormatStyle.LONG))
            .put("applicantName", getFirstApplicantName(caseData.getApplicants()))
            .put("orderTypes", getOrderTypes(caseData.getOrders()))
            .put("childrenNames", getAllChildrenNames(caseData.getAllChildren()))
            .put("judgeTitleAndName", formatJudgeTitleAndName(caseData.getJudgeAndLegalAdvisor()))
            .put("legalAdvisorName", getLegalAdvisorName(caseData.getJudgeAndLegalAdvisor()))
            .putAll(hearingBookingData)
            .build();
    }

    private String getCourtName(String courtName) {
        return hmctsCourtLookupConfiguration.getCourt(courtName).getName();
    }

    private String getLegalAdvisorName(JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        if (judgeAndLegalAdvisor == null) {
            return "";
        }

        return defaultIfNull(judgeAndLegalAdvisor.getLegalAdvisorName(), "");
    }

    private String formatJudgeTitleAndName(JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        if (judgeAndLegalAdvisor == null || judgeAndLegalAdvisor.getJudgeTitle() == null) {
            return "";
        }

        if (judgeAndLegalAdvisor.getJudgeTitle() == MAGISTRATES) {
            return judgeAndLegalAdvisor.getJudgeFullName() + " (JP)";
        } else {
            return judgeAndLegalAdvisor.getJudgeTitle().getLabel() + " " + judgeAndLegalAdvisor.getJudgeLastName();
        }
    }

    private Map<String, String> getHearingBookingData(List<Element<HearingBooking>> hearingBookings) {
        HearingBooking prioritisedHearingBooking = hearingBookingService.getMostUrgentHearingBooking(hearingBookings);

        return ImmutableMap.of(
            "hearingDate", dateFormatterService.formatLocalDateToString(prioritisedHearingBooking.getDate(),
                FormatStyle.LONG),
            "hearingVenue", prioritisedHearingBooking.getVenue(),
            "preHearingAttendance", prioritisedHearingBooking.getPreHearingAttendance(),
            "hearingTime", prioritisedHearingBooking.getTime()
        );
    }

    private String getOrderTypes(Orders orders) {
        return orders.getOrderType().stream()
            .map(orderType -> orderType.getLabel())
            .collect(Collectors.joining(", "));
    }

    private String getFirstApplicantName(List<Element<Applicant>> applicants) {
        return applicants.stream()
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .map(Applicant::getParty)
            .filter(Objects::nonNull)
            .map(ApplicantParty::getOrganisationName)
            .findFirst()
            .orElse("");
    }

    private String getAllChildrenNames(List<Element<Child>> children) {
        String childrenNames = children.stream()
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .map(Child::getParty)
            .filter(Objects::nonNull)
            .map(childParty -> (childParty.getFirstName()) + " " + (childParty.getLastName()))
            .collect(Collectors.joining(", "));

        if (childrenNames.contains(",")) {
            StringBuilder stringBuilder = new StringBuilder(childrenNames);
            stringBuilder.replace(childrenNames.lastIndexOf(","),
                childrenNames.lastIndexOf(",") + 1, " and");

            childrenNames = stringBuilder.toString();
        }

        return childrenNames;
    }
}
