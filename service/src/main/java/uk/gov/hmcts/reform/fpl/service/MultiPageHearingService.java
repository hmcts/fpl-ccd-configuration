package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfHearing;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.docmosis.NoticeOfHearingGenerationService;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.time.LocalDate.now;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.NOTICE_OF_HEARING;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MultiPageHearingService {
    private final NoticeOfHearingGenerationService noticeOfHearingGenerationService;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;

    public HearingBooking findHearingBooking(UUID id, List<Element<HearingBooking>> hearingBookings) {
        Optional<Element<HearingBooking>> hearingBookingElement = ElementUtils.findElement(id, hearingBookings);

        if (hearingBookingElement.isPresent()) {
            return hearingBookingElement.get().getValue();
        }

        return HearingBooking.builder().build();
    }

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

    public List<Element<HearingBooking>> updateEditedHearingEntry(HearingBooking hearingBooking,
                                                                  UUID hearingId,
                                                                  List<Element<HearingBooking>> hearings) {
        return hearings.stream()
            .map(hearingBookingElement -> {
                if (hearingBookingElement.getId().equals(hearingId)) {
                    hearingBookingElement = Element.<HearingBooking>builder()
                        .id(hearingBookingElement.getId())
                        .value(hearingBooking)
                        .build();
                }
                return hearingBookingElement;
            }).collect(Collectors.toList());
    }

    public List<Element<HearingBooking>> appendHearingBooking(List<Element<HearingBooking>> currentHearingBookings,
                                                              HearingBooking hearingBooking) {
        Element<HearingBooking> hearingBookingElement = Element.<HearingBooking>builder()
            .id(UUID.randomUUID())
            .value(hearingBooking)
            .build();

        if (currentHearingBookings.isEmpty()) {
            return List.of(hearingBookingElement);
        }

        currentHearingBookings.add(hearingBookingElement);
        return currentHearingBookings;
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
            .build();
    }
}
