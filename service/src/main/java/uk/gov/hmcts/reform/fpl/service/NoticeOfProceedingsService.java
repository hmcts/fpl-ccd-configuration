package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper;

import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfProceedingsService {
    private final HearingBookingService hearingBookingService;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final HearingVenueLookUpService hearingVenueLookUpService;
    private final CommonCaseDataExtractionService commonCaseDataExtractionService;

    public List<Element<DocumentBundle>> getRemovedDocumentBundles(CaseData caseData,
                                                                   List<DocmosisTemplates> templateTypes) {
        List<String> templateTypeTitles = templateTypes.stream().map(DocmosisTemplates::getDocumentTitle)
            .collect(Collectors.toList());

        ImmutableList.Builder<Element<DocumentBundle>> removedDocumentBundles = ImmutableList.builder();

        caseData.getNoticeOfProceedingsBundle().forEach(element -> {
            String filename = element.getValue().getDocument().getFilename();

            if (!templateTypeTitles.contains(filename)) {
                removedDocumentBundles.add(element);
            }
        });

        return removedDocumentBundles.build();
    }

    public Map<String, Object> getNoticeOfProceedingTemplateData(CaseData caseData) {
        Map<String, Object> hearingBookingData = getHearingBookingData(caseData.getHearingDetails());

        // Validation within our frontend ensures that the following data is present
        return ImmutableMap.<String, Object>builder()
            .put("courtName", getCourtName(caseData.getCaseLocalAuthority()))
            .put("familyManCaseNumber", caseData.getFamilyManCaseNumber())
            .put("todaysDate", formatLocalDateToString(LocalDate.now(), FormatStyle.LONG))
            .put("applicantName", getFirstApplicantName(caseData.getApplicants()))
            .put("orderTypes", getOrderTypes(caseData.getOrders()))
            .put("childrenNames", getAllChildrenNames(caseData.getAllChildren()))
            .put("judgeTitleAndName", JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName(
                caseData.getNoticeOfProceedings().getJudgeAndLegalAdvisor()))
            .put("legalAdvisorName", JudgeAndLegalAdvisorHelper.getLegalAdvisorName(
                caseData.getNoticeOfProceedings().getJudgeAndLegalAdvisor()))
            .putAll(hearingBookingData)
            .build();
    }

    private String getCourtName(String courtName) {
        return hmctsCourtLookupConfiguration.getCourt(courtName).getName();
    }

    private Map<String, Object>  getHearingBookingData(List<Element<HearingBooking>> hearingBookings) {
        HearingBooking prioritisedHearingBooking = hearingBookingService.getMostUrgentHearingBooking(hearingBookings);
        HearingVenue hearingVenue = hearingVenueLookUpService.getHearingVenue(prioritisedHearingBooking.getVenue());

        return ImmutableMap.of(
            "hearingDate", commonCaseDataExtractionService.getHearingDateIfHearingsOnSameDay(
                prioritisedHearingBooking)
                .orElse(""),
            "hearingVenue", hearingVenueLookUpService.buildHearingVenue(hearingVenue),
            "preHearingAttendance", commonCaseDataExtractionService.extractPrehearingAttendance(
                prioritisedHearingBooking),
            "hearingTime", commonCaseDataExtractionService.getHearingTime(prioritisedHearingBooking)
        );
    }

    private String getOrderTypes(Orders orders) {
        return orders.getOrderType().stream()
            .map(OrderType::getLabel)
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
