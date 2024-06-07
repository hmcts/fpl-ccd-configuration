package uk.gov.hmcts.reform.fpl.service.docmosis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisHearingBooking;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfHearing;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfHearingVacated;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.format.FormatStyle;

import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.CREST;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.service.email.content.HearingVacatedEmailContentProvider.RELIST_ACTION_NOT_RELISTED;
import static uk.gov.hmcts.reform.fpl.service.email.content.HearingVacatedEmailContentProvider.RELIST_ACTION_RELISTED;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.formatCCDCaseNumber;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getSelectedJudge;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfHearingGenerationService {
    private final CaseDataExtractionService dataService;
    private final CourtService courtService;

    private final Time time;

    public DocmosisNoticeOfHearing getTemplateData(CaseData caseData, HearingBooking hearingBooking) {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = getSelectedJudge(hearingBooking.getJudgeAndLegalAdvisor(),
            caseData.getAllocatedJudge());

        return DocmosisNoticeOfHearing.builder()
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .ccdCaseNumber(formatCCDCaseNumber(caseData.getId()))
            .courtName(courtService.getCourtName(caseData))
            .children(dataService.getChildrenDetails(caseData.getChildren1()))
            .hearingBooking(getHearingBooking(hearingBooking))
            .judgeAndLegalAdvisor(dataService.getJudgeAndLegalAdvisor(judgeAndLegalAdvisor))
            .postingDate(formatLocalDateToString(time.now().toLocalDate(), DATE))
            .additionalNotes(hearingBooking.getAdditionalNotes())
            .courtseal(courtService.getCourtSeal(caseData, SEALED))
            .isHighCourtCase(courtService.isHighCourtCase(caseData))
            .crest(CREST.getValue())
            .build();
    }

    public DocmosisNoticeOfHearingVacated getHearingVacatedTemplateData(CaseData caseData,
                                                                        HearingBooking hearingBooking,
                                                                        boolean isRelisted) {
        return DocmosisNoticeOfHearingVacated.builder()
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .ccdCaseNumber(formatCCDCaseNumber(caseData.getId()))
            .hearingBooking(getHearingBooking(hearingBooking))
            .vacatedDate(formatLocalDateToString(hearingBooking.getVacatedDate(), FormatStyle.LONG))
            .vacatedReason(hearingBooking.getCancellationReason())
            .relistAction(isRelisted ? RELIST_ACTION_RELISTED : RELIST_ACTION_NOT_RELISTED)
            .crest(CREST.getValue())
            .build();
    }

    private DocmosisHearingBooking getHearingBooking(HearingBooking hearingBooking) {
        return dataService.getHearingBookingData(hearingBooking).toBuilder()
            .hearingType(hearingBooking.getType().getLabel().toLowerCase())
            .hearingJudgeTitleAndName(null) // wipe unnecessary fields
            .hearingLegalAdvisorName(null)
            .build();
    }

}
