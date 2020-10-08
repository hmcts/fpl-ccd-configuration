package uk.gov.hmcts.reform.fpl.service.docmosis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfHearing;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.COURT_SEAL;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.CREST;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.OTHER;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfHearingGenerationService {
    private final CaseDataExtractionService dataService;
    private final HearingVenueLookUpService hearingVenueLookUpService;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final FeatureToggleService featureToggleService;

    public DocmosisNoticeOfHearing getTemplateData(CaseData caseData, HearingBooking hearingBooking) {
        HearingVenue venue = hearingVenueLookUpService.getHearingVenue(hearingBooking);

        return DocmosisNoticeOfHearing.builder()
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .courtName(hmctsCourtLookupConfiguration.getCourt(caseData.getCaseLocalAuthority()).getName())
            .children(dataService.getChildrenDetails(caseData.getChildren1()))
            .hearingDate(dataService.getHearingDateIfHearingsOnSameDay(hearingBooking).orElse(""))
            .hearingTime(dataService.getHearingTime(hearingBooking))
            .hearingType(getHearingType(hearingBooking))
            .hearingVenue(hearingVenueLookUpService.buildHearingVenue(venue))
            .preHearingAttendance(dataService.extractPrehearingAttendance(hearingBooking))
            .judgeAndLegalAdvisor(dataService.getJudgeAndLegalAdvisor(hearingBooking.getJudgeAndLegalAdvisor()))
            .postingDate(formatLocalDateToString(LocalDate.now(), DATE))
            .additionalNotes(getHearingNotes(caseData.getNoticeOfHearingNotes(), hearingBooking.getAdditionalNotes()))
            .courtseal(COURT_SEAL.getValue())
            .crest(CREST.getValue())
            .build();
    }

    private String getHearingType(HearingBooking hearingBooking) {
        return hearingBooking.getType() != OTHER ? hearingBooking.getType().getLabel().toLowerCase() :
            hearingBooking.getTypeDetails();
    }

    //feature toggle
    private String getHearingNotes(String multiHearingNotes, String singleHearingNotes) {
        if (featureToggleService.isMultiPageHearingEnabled()) {
            return multiHearingNotes;
        } else {
            return singleHearingNotes;
        }
    }
}
