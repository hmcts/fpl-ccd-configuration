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
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;

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
        Map<String, String> hearingBookingData = getHearingBookingData(caseData);

        // Validation within our frontend ensures that the following data is present

        return ImmutableMap.<String, String>builder()
            .put("courtName", getCourtName(caseData))
            .put("familyManCaseNumber", caseData.getFamilyManCaseNumber())
            .put("todaysDate", dateFormatterService.formatLocalDateToString(LocalDate.now(), FormatStyle.LONG))
            .put("applicantName", getFirstApplicantName(caseData))
            .put("orderTypes", getOrderTypes(caseData))
            .put("childrenNames", getAllChildrenNames(caseData))
            .put("judgeTitleAndName", formatJudgeTitleAndName(caseData))
            .put("legalAdvisorName", caseData.getJudgeAndLegalAdvisor() == null
                || caseData.getJudgeAndLegalAdvisor().getLegalAdvisorName() == null ? ""
                : caseData.getJudgeAndLegalAdvisor().getLegalAdvisorName())
            .putAll(hearingBookingData)
            .build();
    }

    private String getCourtName(CaseData caseData) {
        return hmctsCourtLookupConfiguration.getCourt(caseData.getCaseLocalAuthority()).getName();
    }

    private String formatJudgeTitleAndName(CaseData caseData) {
        if (caseData.getJudgeAndLegalAdvisor() == null || caseData.getJudgeAndLegalAdvisor().getJudgeTitle() == null) {
            return "";
        }

        JudgeAndLegalAdvisor judgeOrMagistrateTitle = caseData.getJudgeAndLegalAdvisor();

        if (caseData.getJudgeAndLegalAdvisor().getJudgeTitle() == MAGISTRATES) {
            return judgeOrMagistrateTitle.getJudgeFullName() + " (JP)";
        } else {
            return judgeOrMagistrateTitle.getJudgeTitle().getLabel() + " " + judgeOrMagistrateTitle.getJudgeLastName();
        }
    }

    private Map<String, String> getHearingBookingData(CaseData caseData) {
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

    private String getAllChildrenNames(CaseData caseData) {
        String childrenNames = caseData.getAllChildren().stream()
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
