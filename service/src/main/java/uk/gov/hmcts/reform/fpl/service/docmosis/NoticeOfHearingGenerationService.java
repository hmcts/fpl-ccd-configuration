package uk.gov.hmcts.reform.fpl.service.docmosis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisHearingBooking;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfHearing;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.COURT_SEAL;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.CREST;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.OTHER;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.formatCCDCaseNumber;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getSelectedJudge;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfHearingGenerationService {
    private final CaseDataExtractionService dataService;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final Time time;

    public DocmosisNoticeOfHearing getTemplateData(CaseData caseData, HearingBooking hearingBooking) {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = getSelectedJudge(hearingBooking.getJudgeAndLegalAdvisor(),
            caseData.getAllocatedJudge());

        return DocmosisNoticeOfHearing.builder()
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .ccdCaseNumber(formatCCDCaseNumber(caseData.getId()))
            .courtName(hmctsCourtLookupConfiguration.getCourt(caseData.getCaseLocalAuthority()).getName())
            .children(dataService.getChildrenDetails(caseData.getChildren1()))
            .hearingBooking(getHearingBooking(hearingBooking))
            .judgeAndLegalAdvisor(dataService.getJudgeAndLegalAdvisor(judgeAndLegalAdvisor))
            .postingDate(formatLocalDateToString(time.now().toLocalDate(), DATE))
            .additionalNotes(hearingBooking.getAdditionalNotes())
            .courtseal(COURT_SEAL.getValue())
            .crest(CREST.getValue())
            .build();
    }

    private DocmosisHearingBooking getHearingBooking(HearingBooking hearingBooking) {
        return dataService.getHearingBookingData(hearingBooking).toBuilder()
            .hearingType(getHearingType(hearingBooking))
            .hearingJudgeTitleAndName(null) // wipe unnecessary fields
            .hearingLegalAdvisorName(null)
            .build();
    }

    private String getHearingType(HearingBooking hearingBooking) {
        return hearingBooking.getType() != OTHER ? hearingBooking.getType().getLabel().toLowerCase() :
            hearingBooking.getTypeDetails();
    }

}
