package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;
import uk.gov.hmcts.reform.fpl.model.PreviousHearingVenue;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfHearing;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.docmosis.NoticeOfHearingGenerationService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.NOTICE_OF_HEARING;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;

@ExtendWith(MockitoExtension.class)
public class ManageHearingsServiceTest {

    private static final String VENUE = "31";
    private static final HearingVenue HEARING_VENUE = HearingVenue.builder()
        .hearingVenueId(VENUE)
        .venue("some place")
        .build();
    private static final Address VENUE_CUSTOM_ADDRESS = Address.builder()
        .addressLine1("custom")
        .addressLine2("address")
        .build();
    private static final Time time = new FixedTimeConfiguration().stoppedTime();
    private static final Document DOCUMENT = testDocument();

    @Mock
    private HearingVenueLookUpService hearingVenueLookUpService;

    @Mock
    private NoticeOfHearingGenerationService noticeOfHearingGenerationService;

    @Mock
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    @Mock
    private UploadDocumentService uploadDocumentService;

    private ManageHearingsService service;

    @BeforeEach
    void setUp() {
        service = new ManageHearingsService(
            noticeOfHearingGenerationService,
            docmosisDocumentGeneratorService,
            uploadDocumentService,
            hearingVenueLookUpService,
            time
        );
    }

    @Test
    void shouldPullVenueFromPreviousHearingWhenExistingHearingIsInThePast() {
        HearingBooking hearing = hearing(time.now().minusDays(1), time.now());
        given(hearingVenueLookUpService.getHearingVenue(hearing)).willReturn(HEARING_VENUE);

        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(element(hearing), element(hearing(time.now().plusHours(3), time.now()))))
            .build();

        HearingVenue venue = service.getPreviousHearingVenue(caseData);

        assertThat(venue).isEqualTo(HEARING_VENUE);
    }

    @Test
    void shouldPullVenueFromFirstHearingWhenNoneAreInThePast() {
        HearingBooking hearing = hearing(time.now().plusHours(1), time.now().plusHours(4));
        given(hearingVenueLookUpService.getHearingVenue(hearing)).willReturn(HEARING_VENUE);

        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(element(hearing)))
            .build();

        HearingVenue venue = service.getPreviousHearingVenue(caseData);

        assertThat(venue).isEqualTo(HEARING_VENUE);
    }

    @Test
    void shouldReturnHearingWhenHearingWithMatchingIdInList() {
        UUID knownId = UUID.randomUUID();
        HearingBooking hearing = hearing(time.now(), time.now().plusHours(3));

        List<Element<HearingBooking>> hearings = List.of(
            element(hearing(time.now().minusDays(1), time.now().minusHours(3))),
            element(knownId, hearing)
        );

        HearingBooking foundHearing = service.findHearingBooking(knownId, hearings);

        assertThat(foundHearing).isEqualTo(hearing);
    }

    @Test
    void shouldReturnBlankHearingBookingWhenNoIdMatches() {
        List<Element<HearingBooking>> hearings = List.of(
            element(hearing(time.now().minusDays(1), time.now().minusHours(3))),
            element(hearing(time.now().plusHours(3), time.now().plusHours(4)))
        );

        HearingBooking foundHearing = service.findHearingBooking(UUID.randomUUID(), hearings);

        assertThat(foundHearing).isEqualTo(HearingBooking.builder().build());
    }

    //populatePreviousVenueFields

    @Test
    void shouldPullCustomAddressFromHearingWhenHearingVenueIsOther() {
        given(hearingVenueLookUpService.getHearingVenue(any())).willCallRealMethod();
        given(hearingVenueLookUpService.buildHearingVenue(any())).willCallRealMethod();

        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(element(hearingWithCustomAddress(
                time.now().plusHours(1), time.now().plusHours(2)))))
            .build();

        Map<String, Object> previousVenueFields = service.populatePreviousVenueFields(caseData);

        PreviousHearingVenue hearingVenue = PreviousHearingVenue.builder()
            .previousVenue("custom, address")
            .newVenueCustomAddress(VENUE_CUSTOM_ADDRESS)
            .build();

        assertThat(previousVenueFields).hasSize(2)
            .extracting("previousHearingVenue", "previousVenueId")
            .containsOnly(hearingVenue, "OTHER");
    }

    @Test
    void shouldPullVenueAddressFromHearing() {
        HearingBooking hearing = hearing(time.now().minusDays(1), time.now());
        String venueAddress = "some address that is definitely real";

        given(hearingVenueLookUpService.getHearingVenue(hearing)).willReturn(HEARING_VENUE);
        given(hearingVenueLookUpService.buildHearingVenue(HEARING_VENUE)).willReturn(venueAddress);

        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(element(hearing)))
            .build();

        Map<String, Object> previousVenueFields = service.populatePreviousVenueFields(caseData);

        PreviousHearingVenue hearingVenue = PreviousHearingVenue.builder()
            .previousVenue(venueAddress)
            .build();

        assertThat(previousVenueFields).hasSize(2)
            .extracting("previousHearingVenue", "previousVenueId")
            .containsOnly(hearingVenue, "31");

    }

    //populateHearingCaseFields tests (low prio)

    //buildHearingBooking tests

    @Test
    void shouldAddNoticeOfHearingToHearingBooking() {
        mockDocuments();
        HearingBooking hearingToUpdate = hearing(time.now(), time.now().plusHours(1));
        CaseData caseData = CaseData.builder().build();

        service.addNoticeOfHearing(caseData, hearingToUpdate);

        assertThat(hearingToUpdate.getNoticeOfHearing()).isEqualTo(DocumentReference.buildFromDocument(DOCUMENT));
    }

    @Test
    void shouldUpdateExistingHearing() {
        UUID idToUpdate = UUID.randomUUID();
        HearingBooking hearing = hearing(time.now(), time.now().plusHours(1));

        List<Element<HearingBooking>> hearings = List.of(
            element(hearing(time.now().plusDays(1), time.now().plusDays(2))),
            element(idToUpdate, hearing(time.now().plusDays(3), time.now().plusHours(74)))
        );

        List<Element<HearingBooking>> updatedList = service.updateEditedHearingEntry(hearing, idToUpdate, hearings);

        assertThat(updatedList).hasSize(2);
        assertThat(updatedList.get(1)).isEqualTo(element(idToUpdate, hearing));
    }

    private HearingBooking hearing(LocalDateTime start, LocalDateTime end) {
        return HearingBooking.builder()
            .startDate(start)
            .endDate(end)
            .venue(VENUE)
            .build();
    }

    private HearingBooking hearingWithCustomAddress(LocalDateTime start, LocalDateTime end) {
        return HearingBooking.builder()
            .startDate(start)
            .endDate(end)
            .venue("OTHER")
            .venueCustomAddress(VENUE_CUSTOM_ADDRESS)
            .build();
    }

    private void mockDocuments() {
        DocmosisNoticeOfHearing docmosisData = DocmosisNoticeOfHearing.builder().build();
        DocmosisDocument docmosisDocument = testDocmosisDocument(TestDataHelper.DOCUMENT_CONTENT);
        byte[] documentBytes = docmosisDocument.getBytes();

        given(noticeOfHearingGenerationService.getTemplateData(any(CaseData.class), any(HearingBooking.class)))
            .willReturn(docmosisData);
        given(docmosisDocumentGeneratorService.generateDocmosisDocument(docmosisData, NOTICE_OF_HEARING))
            .willReturn(docmosisDocument);
        given(uploadDocumentService.uploadPDF(eq(documentBytes), anyString())).willReturn(DOCUMENT);
    }
}
