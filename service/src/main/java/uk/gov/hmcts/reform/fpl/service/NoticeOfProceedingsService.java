package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
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
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfProceeding;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper;

import java.time.format.FormatStyle;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.COURT_SEAL;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.CREST;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfProceedingsService {
    private final HearingBookingService hearingBookingService;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final HearingVenueLookUpService hearingVenueLookUpService;
    private final CaseDataExtractionService caseDataExtractionService;
    private final Time time;
    private final ObjectMapper mapper;

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

    public DocmosisNoticeOfProceeding getNoticeOfProceedingTemplateData(CaseData caseData) {

        HearingBooking prioritisedHearingBooking = hearingBookingService
            .getMostUrgentHearingBooking(caseData.getHearingDetails());
        HearingVenue hearingVenue = hearingVenueLookUpService.getHearingVenue(prioritisedHearingBooking);

        return DocmosisNoticeOfProceeding.builder()
            .courtName(getCourtName(caseData.getCaseLocalAuthority()))
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .todaysDate(formatLocalDateToString(time.now().toLocalDate(), FormatStyle.LONG))
            .applicantName(getFirstApplicantName(caseData.getApplicants()))
            .orderTypes(getOrderTypes(caseData.getOrders()))
            .childrenNames(getAllChildrenNames(caseData.getAllChildren()))
            .judgeTitleAndName(JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName(
                caseData.getNoticeOfProceedings().getJudgeAndLegalAdvisor()))
            .legalAdvisorName(JudgeAndLegalAdvisorHelper.getLegalAdvisorName(
                caseData.getNoticeOfProceedings().getJudgeAndLegalAdvisor()))
            .hearingDate(caseDataExtractionService.getHearingDateIfHearingsOnSameDay(
                prioritisedHearingBooking)
                .orElse(""))
            .hearingVenue(hearingVenueLookUpService.buildHearingVenue(hearingVenue))
            .preHearingAttendance(caseDataExtractionService.extractPrehearingAttendance(
                prioritisedHearingBooking))
            .hearingTime(caseDataExtractionService.getHearingTime(prioritisedHearingBooking))
            .crest(CREST.getValue())
            .courtseal(COURT_SEAL.getValue())
            .build();
    }

    private String getCourtName(String courtName) {
        return hmctsCourtLookupConfiguration.getCourt(courtName).getName();
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
            stringBuilder.replace(childrenNames.lastIndexOf(','),
                childrenNames.lastIndexOf(',') + 1, " and");

            childrenNames = stringBuilder.toString();
        }

        return childrenNames;
    }
}
