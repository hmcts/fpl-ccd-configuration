package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfHearing;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.docmosis.NoticeOfHearingGenerationService;

import static java.time.LocalDate.now;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.NOTICE_OF_HEARING;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MultiPageHearingService {
    private final NoticeOfHearingGenerationService noticeOfHearingGenerationService;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;

    public HearingBooking buildHearingBooking(CaseData caseData) {
        if (caseData.getPreviousHearingVenue() == null
            || caseData.getPreviousHearingVenue().getUsePreviousVenue() == null) {
            return buildFirstHearing(caseData);
        } else {
            return buildFollowingHearings(caseData);
        }
    }

    public void addNoticeOfHearing(CaseData caseData, HearingBooking hearingBooking) {
        DocmosisNoticeOfHearing notice = noticeOfHearingGenerationService.getTemplateData(caseData, hearingBooking);
        DocmosisDocument docmosisDocument = docmosisDocumentGeneratorService.generateDocmosisDocument(notice,
            NOTICE_OF_HEARING);
        Document document = uploadDocumentService.uploadPDF(docmosisDocument.getBytes(),
            NOTICE_OF_HEARING.getDocumentTitle(now()));

        hearingBooking.setNoticeOfHearing(DocumentReference.buildFromDocument(document));
    }

    private HearingBooking buildFirstHearing(CaseData caseData) {
        return HearingBooking.builder()
            .type(caseData.getHearingType())
            .venue(caseData.getHearingVenue())
            .venueCustomAddress(caseData.getHearingVenueCustom())
            .startDate(caseData.getHearingStartDate())
            .endDate(caseData.getHearingEndDate())
            .judgeAndLegalAdvisor(caseData.getJudgeAndLegalAdvisor())
            .additionalNotes(caseData.getNoticeOfHearingNotes())
            .build();
    }

    private HearingBooking buildFollowingHearings(CaseData caseData) {
        return HearingBooking.builder()
            .type(caseData.getHearingType())
            .venue(caseData.getPreviousHearingVenue().getUsePreviousVenue().equals("Yes")
                ? caseData.getPreviousVenueId() : caseData.getPreviousHearingVenue().getNewVenue())
            .venueCustomAddress(caseData.getPreviousHearingVenue().getVenueCustomAddress())
            .startDate(caseData.getHearingStartDate())
            .endDate(caseData.getHearingEndDate())
            .judgeAndLegalAdvisor(caseData.getJudgeAndLegalAdvisor())
            .previousHearingVenue(caseData.getPreviousHearingVenue())
            .build();
    }
}
