package uk.gov.hmcts.reform.fpl.service.docmosis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfHearing;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.COURT_SEAL;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.CREST;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfHearingGenerationService {
    private final CaseDataExtractionService dataService;
    private final HearingVenueLookUpService hearingVenueLookUpService;

    public DocmosisNoticeOfHearing getTemplateData(CaseData caseData, HearingBooking hearingBooking) {
        HearingVenue venue = hearingVenueLookUpService.getHearingVenue(hearingBooking);

        return DocmosisNoticeOfHearing.builder()
            .children(dataService.getChildrenDetails(caseData.getChildren1()))
            .hearingDate(dataService.getHearingDateIfHearingsOnSameDay(hearingBooking).orElse(""))
            .hearingTime(dataService.getHearingTime(hearingBooking))
            .hearingType(hearingBooking.getType().getLabel().toLowerCase())
            .hearingVenue(hearingVenueLookUpService.buildHearingVenue(venue))
            .preHearingAttendance(dataService.extractPrehearingAttendance(hearingBooking))
            .judgeAndLegalAdvisor(dataService.getJudgeAndLegalAdvisor(hearingBooking.getJudgeAndLegalAdvisor()))
            .postingDate(formatLocalDateToString(LocalDate.now(), DATE))
            .courtseal(COURT_SEAL.getValue())
            .crest(CREST.getValue())
            .build();
    }

}
