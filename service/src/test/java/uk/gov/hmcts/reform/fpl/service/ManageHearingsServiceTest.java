package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.HearingStatus;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.exceptions.NoHearingBookingException;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingCancellationReason;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.PreviousHearingVenue;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfHearing;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.docmosis.NoticeOfHearingGenerationService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.NOTICE_OF_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.ADJOURN_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.VACATE_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.FUTURE_HEARING_LIST;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.HAS_EXISTING_HEARINGS_FLAG;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.HAS_FUTURE_HEARING_FLAG;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.HAS_HEARINGS_TO_ADJOURN;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.HAS_HEARINGS_TO_VACATE;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.HEARING_DATE_LIST;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.PAST_HEARING_LIST;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testJudge;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testJudgeAndLegalAdviser;

@ExtendWith(MockitoExtension.class)
class ManageHearingsServiceTest {

    private static final String VENUE = "31";
    private static final HearingVenue HEARING_VENUE = HearingVenue.builder()
        .hearingVenueId(VENUE)
        .venue("some place")
        .build();
    private static final Address VENUE_CUSTOM_ADDRESS = Address.builder()
        .addressLine1("custom")
        .addressLine2("address")
        .build();
    private static final Document DOCUMENT = testDocument();

    private static final LocalDateTime NOW = LocalDateTime.now();

    @Mock(lenient = true)
    private Time time;

    @Mock
    private HearingVenueLookUpService hearingVenueLookUpService;

    @Mock
    private NoticeOfHearingGenerationService noticeOfHearingGenerationService;

    @Mock
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    @Mock
    private UploadDocumentService uploadDocumentService;

    @Mock
    private IdentityService identityService;

    @InjectMocks
    private ManageHearingsService service;

    @BeforeEach
    void setUp() {
        when(time.now()).thenReturn(NOW);
    }

    @Nested
    class PopulatePastAndFutureHearingListsTest {
        @Test
        void shouldPopulateAllHearingDateListsWhenPastAndFutureHearingsExist() {
            Element<HearingBooking> futureHearing1 = hearingFromToday(3);
            Element<HearingBooking> futureHearing2 = hearingFromToday(2);
            Element<HearingBooking> todayHearing = hearingFromToday(0);
            Element<HearingBooking> pastHearing1 = hearingFromToday(-2);
            Element<HearingBooking> pastHearing2 = hearingFromToday(-3);

            Object expectedHearingList = asDynamicList(List.of(
                futureHearing1, futureHearing2), HearingBooking::toLabel);
            Object expectedPastHearingList = asDynamicList(List.of(
                todayHearing, pastHearing1, pastHearing2), HearingBooking::toLabel);
            Object expectedFutureHearingList = asDynamicList(List.of(
                futureHearing1, futureHearing2, todayHearing), HearingBooking::toLabel);

            CaseData initialCaseData = CaseData.builder()
                .hearingDetails(List.of(futureHearing1, futureHearing2, todayHearing, pastHearing1, pastHearing2))
                .build();

            Map<String, Object> data = service.populatePastAndFutureHearingLists(initialCaseData);

            assertThat(data)
                .containsEntry(HAS_HEARINGS_TO_ADJOURN, "Yes")
                .containsEntry(HAS_FUTURE_HEARING_FLAG, "Yes")
                .containsEntry(HAS_HEARINGS_TO_VACATE, "Yes")
                .containsEntry(HAS_EXISTING_HEARINGS_FLAG, "Yes")
                .containsEntry(HEARING_DATE_LIST, expectedHearingList)
                .containsEntry(PAST_HEARING_LIST, expectedPastHearingList)
                .containsEntry(FUTURE_HEARING_LIST, expectedFutureHearingList);
        }

        @Test
        void shouldOnlyPopulatePastHearingDateListWhenOnlyHearingsInThePastExist() {
            Element<HearingBooking> pastHearing1 = hearingFromToday(-2);
            Element<HearingBooking> pastHearing2 = hearingFromToday(-3);

            Object expectedPastHearingList = asDynamicList(List.of(
                pastHearing1, pastHearing2), HearingBooking::toLabel);

            Object emptyDynamicList = asDynamicList(List.of(), HearingBooking::toLabel);

            CaseData initialCaseData = CaseData.builder()
                .hearingDetails(List.of(pastHearing1, pastHearing2))
                .build();

            Map<String, Object> data = service.populatePastAndFutureHearingLists(initialCaseData);

            assertThat(data)
                .containsEntry(HAS_HEARINGS_TO_ADJOURN, "Yes")
                .containsEntry(HAS_EXISTING_HEARINGS_FLAG, "Yes")
                .containsEntry(HEARING_DATE_LIST, emptyDynamicList)
                .containsEntry(PAST_HEARING_LIST, expectedPastHearingList);
        }

        @Test
        void shouldOnlyPopulateFutureHearingDateListWhenOnlyHearingsInTheFutureExist() {
            Element<HearingBooking> futureHearing1 = hearingFromToday(3);
            Element<HearingBooking> futureHearing2 = hearingFromToday(2);

            Object expectedHearingList = asDynamicList(List.of(
                futureHearing1, futureHearing2), HearingBooking::toLabel);
            Object expectedFutureHearingList = asDynamicList(List.of(
                futureHearing1, futureHearing2), HearingBooking::toLabel);
            Object emptyDynamicList = asDynamicList(List.of(), HearingBooking::toLabel);

            CaseData initialCaseData = CaseData.builder()
                .hearingDetails(List.of(futureHearing1, futureHearing2))
                .build();

            Map<String, Object> data = service.populatePastAndFutureHearingLists(initialCaseData);

            assertThat(data)
                .containsEntry(HAS_FUTURE_HEARING_FLAG, "Yes")
                .containsEntry(HAS_HEARINGS_TO_VACATE, "Yes")
                .containsEntry(HAS_EXISTING_HEARINGS_FLAG, "Yes")
                .containsEntry(HEARING_DATE_LIST, expectedHearingList)
                .containsEntry(PAST_HEARING_LIST, emptyDynamicList)
                .containsEntry(FUTURE_HEARING_LIST, expectedFutureHearingList);
        }

        private Element<HearingBooking> hearingFromToday(int daysFromToday) {
            final LocalDateTime startTime = LocalDateTime.now().plusDays(daysFromToday);
            return element(HearingBooking.builder()
                .type(CASE_MANAGEMENT)
                .startDate(startTime)
                .endDate(startTime.plusDays(1))
                .build());
        }
    }

    @Nested
    class GetHearingVenue {

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
    }

    @Nested
    class FindHearingBooking {

        @Test
        void shouldReturnExistingHearingBooking() {
            Element<HearingBooking> hearing1 = element(randomHearing());
            Element<HearingBooking> hearing2 = element(randomHearing());

            List<Element<HearingBooking>> hearings = List.of(hearing1, hearing2);

            assertThat(service.findHearingBooking(hearing2.getId(), hearings)).contains(hearing2.getValue());
        }

        @Test
        void shouldReturnEmptyWhenHearingBookingDoesNotExists() {
            List<Element<HearingBooking>> hearings = wrapElements(randomHearing(), randomHearing());

            assertThat(service.findHearingBooking(randomUUID(), hearings)).isNotPresent();
        }
    }

    @Nested
    class GetHearingBooking {

        @Test
        void shouldReturnExistingHearingBooking() {
            Element<HearingBooking> hearing1 = element(randomHearing());
            Element<HearingBooking> hearing2 = element(randomHearing());

            List<Element<HearingBooking>> hearings = List.of(hearing1, hearing2);

            assertThat(service.getHearingBooking(hearing2.getId(), hearings)).isEqualTo(hearing2.getValue());
        }

        @Test
        void shouldThrowExceptionWhenHearingBookingDoesNotExists() {
            UUID nonExistingHearingId = randomUUID();
            List<Element<HearingBooking>> hearings = wrapElements(randomHearing(), randomHearing());

            final NoHearingBookingException exception = assertThrows(NoHearingBookingException.class,
                () -> service.getHearingBooking(nonExistingHearingId, hearings));

            assertThat(exception).hasMessage(format("Hearing booking with id %s not found", nonExistingHearingId));
        }
    }

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

        assertThat(previousVenueFields).hasSize(1)
            .extracting("previousHearingVenue").isEqualTo(hearingVenue);
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

        assertThat(previousVenueFields).hasSize(1)
            .extracting("previousHearingVenue").isEqualTo((hearingVenue));

    }

    @Test
    void shouldUnwrapHearingIntoSeparateFields() {
        LocalDateTime startDate = time.now().plusDays(1);
        LocalDateTime endDate = time.now().plusHours(25);
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = testJudgeAndLegalAdviser();
        PreviousHearingVenue previousHearingVenue = PreviousHearingVenue.builder().previousVenue("prev venue").build();

        HearingBooking hearing = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .venue("OTHER")
            .venueCustomAddress(VENUE_CUSTOM_ADDRESS)
            .startDate(startDate)
            .endDate(endDate)
            .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
            .previousHearingVenue(previousHearingVenue)
            .build();

        Map<String, Object> hearingCaseFields = service.populateHearingCaseFields(hearing, null);

        Map<String, Object> expectedCaseFields = Map.of(
            "hearingType", CASE_MANAGEMENT,
            "hearingStartDate", startDate,
            "hearingEndDate", endDate,
            "judgeAndLegalAdvisor", judgeAndLegalAdvisor,
            "previousHearingVenue", previousHearingVenue
        );

        assertThat(hearingCaseFields).containsExactlyInAnyOrderEntriesOf(expectedCaseFields);
    }

    @Test
    void shouldUnwrapHearingWhenNoPreviousVenueAndCustomHearingTypeUsedAndAllocatedJudgeUsed() {
        LocalDateTime startDate = time.now().plusDays(1);
        LocalDateTime endDate = time.now().plusHours(25);
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = testJudgeAndLegalAdviser();
        Judge allocatedJudge = testJudge();

        HearingBooking hearing = HearingBooking.builder()
            .type(OTHER)
            .typeDetails("Fact finding")
            .venue("OTHER")
            .venueCustomAddress(VENUE_CUSTOM_ADDRESS)
            .startDate(startDate)
            .endDate(endDate)
            .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
            .previousHearingVenue(PreviousHearingVenue.builder().build())
            .build();

        Map<String, Object> hearingCaseFields = service.populateHearingCaseFields(hearing, allocatedJudge);

        Map<String, Object> expectedCaseFields = Map.of(
            "hearingType", OTHER,
            "hearingTypeDetails", "Fact finding",
            "hearingStartDate", startDate,
            "hearingEndDate", endDate,
            "judgeAndLegalAdvisor", judgeAndLegalAdvisor,
            "hearingVenue", "OTHER",
            "hearingVenueCustom", VENUE_CUSTOM_ADDRESS
        );

        assertThat(hearingCaseFields).containsExactlyInAnyOrderEntriesOf(expectedCaseFields);
    }

    @Test
    void shouldBuildHearingBookingWhenNoPreviousVenueExists() {
        LocalDateTime startDate = time.now();
        LocalDateTime endDate = time.now().plusHours(1);

        CaseData caseData = CaseData.builder()
            .hearingType(CASE_MANAGEMENT)
            .hearingVenue(VENUE)
            .hearingStartDate(startDate)
            .hearingEndDate(endDate)
            .judgeAndLegalAdvisor(testJudgeAndLegalAdviser())
            .noticeOfHearingNotes("notes")
            .build();

        HearingBooking hearingBooking = service.getCurrentHearingBooking(caseData);

        HearingBooking expectedHearingBooking = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .venue(VENUE)
            .startDate(startDate)
            .endDate(endDate)
            .hearingJudgeLabel("Her Honour Judge Judy")
            .legalAdvisorLabel(testJudgeAndLegalAdviser().getLegalAdvisorName())
            .judgeAndLegalAdvisor(testJudgeAndLegalAdviser())
            .additionalNotes("notes")
            .build();

        assertThat(hearingBooking).isEqualTo(expectedHearingBooking);
    }

    @Test
    void shouldNotUsePreviousVenueToBuildHearingBookingWhenFlagIsNo() {
        LocalDateTime startDate = time.now();
        LocalDateTime endDate = time.now().plusHours(1);
        PreviousHearingVenue previousHearingVenue = PreviousHearingVenue.builder()
            .newVenue(VENUE)
            .usePreviousVenue("No")
            .build();

        CaseData caseData = CaseData.builder()
            .hearingType(CASE_MANAGEMENT)
            .hearingStartDate(startDate)
            .hearingEndDate(endDate)
            .judgeAndLegalAdvisor(testJudgeAndLegalAdviser())
            .noticeOfHearingNotes("notes")
            .previousHearingVenue(previousHearingVenue)
            .build();

        HearingBooking hearingBooking = service.getCurrentHearingBooking(caseData);

        HearingBooking expectedHearingBooking = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .venue(VENUE)
            .startDate(startDate)
            .endDate(endDate)
            .hearingJudgeLabel("Her Honour Judge Judy")
            .legalAdvisorLabel(testJudgeAndLegalAdviser().getLegalAdvisorName())
            .judgeAndLegalAdvisor(testJudgeAndLegalAdviser())
            .additionalNotes("notes")
            .previousHearingVenue(previousHearingVenue)
            .build();

        assertThat(hearingBooking).isEqualTo(expectedHearingBooking);
    }

    @Test
    void shouldUsePreviousVenueToBuildHearingBookingWhenFlagIsSetToYes() {
        LocalDateTime startDate = time.now();
        LocalDateTime endDate = time.now().plusHours(1);
        PreviousHearingVenue previousHearingVenue = PreviousHearingVenue.builder()
            .previousVenue("Custom House, Custom Street")
            .usePreviousVenue("Yes")
            .build();

        CaseData caseData = CaseData.builder()
            .previousVenueId("OTHER")
            .hearingType(CASE_MANAGEMENT)
            .hearingStartDate(startDate)
            .hearingEndDate(endDate)
            .judgeAndLegalAdvisor(testJudgeAndLegalAdviser())
            .noticeOfHearingNotes("notes")
            .previousHearingVenue(previousHearingVenue)
            .build();

        HearingBooking hearingBooking = service.getCurrentHearingBooking(caseData);

        HearingBooking expectedHearingBooking = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .venue("OTHER")
            .customPreviousVenue("Custom House, Custom Street")
            .startDate(startDate)
            .endDate(endDate)
            .hearingJudgeLabel("Her Honour Judge Judy")
            .legalAdvisorLabel(testJudgeAndLegalAdviser().getLegalAdvisorName())
            .judgeAndLegalAdvisor(testJudgeAndLegalAdviser())
            .additionalNotes("notes")
            .previousHearingVenue(previousHearingVenue)
            .build();

        assertThat(hearingBooking).isEqualTo(expectedHearingBooking);
    }

    @Test
    void shouldFindAndSetPreviousVenueIdWhenFlagSet() {
        PreviousHearingVenue previousHearingVenue = PreviousHearingVenue.builder()
            .previousVenue("Custom House, Custom Street")
            .usePreviousVenue("Yes")
            .build();

        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(element(HearingBooking.builder().build())))
            .previousHearingVenue(previousHearingVenue)
            .build();

        service.findAndSetPreviousVenueId(caseData);

        verify(hearingVenueLookUpService).getVenueId(previousHearingVenue.getPreviousVenue());
    }

    @Test
    void shouldFindAndSetPreviousVenueIdWhenFlagNotSet() {
        PreviousHearingVenue previousHearingVenue = PreviousHearingVenue.builder()
            .previousVenue("Custom House, Custom Street")
            .usePreviousVenue("No")
            .build();

        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(element(HearingBooking.builder().build())))
            .previousHearingVenue(previousHearingVenue)
            .build();

        service.findAndSetPreviousVenueId(caseData);

        assertThat(caseData.getPreviousVenueId()).isNull();
    }

    @Test
    void shouldNotFindAndSetPreviousVenueIdWhenNoHearings() {
        CaseData caseData = CaseData.builder().build();

        service.findAndSetPreviousVenueId(caseData);

        assertThat(caseData.getPreviousVenueId()).isNull();
    }

    @Test
    void shouldSendNoticeOfHearingIfRequested() {
        final DocmosisNoticeOfHearing docmosisData = DocmosisNoticeOfHearing.builder().build();
        final DocmosisDocument docmosisDocument = testDocmosisDocument(TestDataHelper.DOCUMENT_CONTENT);

        final HearingBooking hearingToUpdate = randomHearing();
        final CaseData caseData = CaseData.builder()
            .sendNoticeOfHearing(YesNo.YES.getValue())
            .build();

        given(noticeOfHearingGenerationService.getTemplateData(caseData, hearingToUpdate))
            .willReturn(docmosisData);
        given(docmosisDocumentGeneratorService.generateDocmosisDocument(docmosisData, NOTICE_OF_HEARING))
            .willReturn(docmosisDocument);
        given(uploadDocumentService.uploadPDF(eq(docmosisDocument.getBytes()), anyString())).willReturn(DOCUMENT);

        service.sendNoticeOfHearing(caseData, hearingToUpdate);

        assertThat(hearingToUpdate.getNoticeOfHearing()).isEqualTo(DocumentReference.buildFromDocument(DOCUMENT));

        verify(noticeOfHearingGenerationService).getTemplateData(caseData, hearingToUpdate);
        verify(docmosisDocumentGeneratorService).generateDocmosisDocument(docmosisData, NOTICE_OF_HEARING);
        verify(uploadDocumentService).uploadPDF(
            TestDataHelper.DOCUMENT_CONTENT,
            NOTICE_OF_HEARING.getDocumentTitle(time.now().toLocalDate()));
    }

    @Test
    void shouldNotSendNoticeOfHearingIfNotRequested() {
        HearingBooking hearingToUpdate = randomHearing();
        CaseData caseData = CaseData.builder()
            .sendNoticeOfHearing(YesNo.NO.getValue())
            .build();

        service.sendNoticeOfHearing(caseData, hearingToUpdate);

        assertThat(hearingToUpdate.getNoticeOfHearing()).isNull();
        verifyNoInteractions(uploadDocumentService, docmosisDocumentGeneratorService, noticeOfHearingGenerationService);
    }

    @Nested
    class AddOrUpdate {

        @Test
        void shouldUpdateExistingHearing() {
            Element<HearingBooking> hearing1 = element(hearing(time.now().plusDays(1), time.now().plusDays(2)));
            Element<HearingBooking> hearing2 = element(hearing(time.now().plusDays(2), time.now().plusDays(3)));
            Element<HearingBooking> updatedHearing = element(hearing1.getId(),
                hearing(time.now().plusDays(4), time.now().plusDays(5)));

            CaseData caseData = CaseData.builder()
                .hearingDetails(List.of(hearing1, hearing2))
                .build();

            service.addOrUpdate(updatedHearing, caseData);

            assertThat(caseData.getHearingDetails()).containsExactly(updatedHearing, hearing2);
        }

        @Test
        void shouldAddNewHearing() {
            Element<HearingBooking> hearing1 = element(hearing(time.now().plusDays(1), time.now().plusDays(2)));
            Element<HearingBooking> hearing2 = element(hearing(time.now().plusDays(2), time.now().plusDays(3)));
            Element<HearingBooking> newHearing = element(hearing(time.now().plusDays(4), time.now().plusDays(5)));

            CaseData caseData = CaseData.builder()
                .hearingDetails(newArrayList(hearing1, hearing2))
                .build();

            service.addOrUpdate(newHearing, caseData);

            assertThat(caseData.getHearingDetails())
                .containsExactly(hearing1, hearing2, newHearing);
        }
    }

    @Nested
    class Adjournment {

        @Test
        void shouldAdjournHearing() {
            HearingCancellationReason adjournmentReason = HearingCancellationReason.builder()
                .reason("Reason 1")
                .build();

            Element<HearingBooking> hearingElement1 = element(hearing(time.now().plusDays(1), time.now().plusDays(2)));
            Element<HearingBooking> hearingElement2 = element(hearing(time.now().plusDays(2), time.now().plusDays(3)));

            Element<HearingBooking> adjournedHearing = element(hearingElement1.getId(),
                hearingElement1.getValue().toBuilder()
                    .status(HearingStatus.ADJOURNED)
                    .cancellationReason(adjournmentReason.getReason())
                    .build());

            CaseData caseData = CaseData.builder()
                .hearingDetails(newArrayList(hearingElement1, hearingElement2))
                .adjournmentReason(adjournmentReason)
                .build();

            service.adjournHearing(caseData, hearingElement1.getId());

            assertThat(caseData.getHearingDetails()).containsExactly(hearingElement2);
            assertThat(caseData.getCancelledHearingDetails()).containsExactly(adjournedHearing);
        }

        @Test
        void shouldAdjournAndReListHearingWithoutDocumentReassignment() {
            final UUID reListedHearingId = randomUUID();

            when(identityService.generateId()).thenReturn(reListedHearingId);

            HearingCancellationReason adjournmentReason = HearingCancellationReason.builder()
                .reason("Reason 1")
                .build();

            Element<HearingBooking> hearingToBeAdjourned = element(randomHearing());
            Element<HearingBooking> otherHearing = element(randomHearing());
            Element<HearingBooking> reListedHearing = element(reListedHearingId, randomHearing());
            Element<HearingBooking> expectedAdjournedHearing = element(hearingToBeAdjourned.getId(),
                hearingToBeAdjourned.getValue().toBuilder()
                    .status(HearingStatus.ADJOURNED_AND_RE_LISTED)
                    .cancellationReason(adjournmentReason.getReason())
                    .build());

            CaseData caseData = CaseData.builder()
                .hearingDetails(newArrayList(hearingToBeAdjourned, otherHearing))
                .adjournmentReason(adjournmentReason)
                .build();

            service.adjournAndReListHearing(caseData, hearingToBeAdjourned.getId(), reListedHearing.getValue());

            assertThat(caseData.getHearingDetails()).containsExactly(otherHearing, reListedHearing);
            assertThat(caseData.getCancelledHearingDetails()).containsExactly(expectedAdjournedHearing);
            assertThat(caseData.getHearingFurtherEvidenceDocuments()).isEmpty();
        }

        @Test
        void shouldAdjournAndReListHearingWithDocumentReassignment() {
            final UUID reListedHearingId = randomUUID();

            when(identityService.generateId()).thenReturn(reListedHearingId);

            HearingCancellationReason adjournmentReason = HearingCancellationReason.builder()
                .reason("Reason 1")
                .build();

            final Element<HearingBooking> hearingToBeAdjourned = element(randomHearing());
            final Element<HearingBooking> otherHearing = element(randomHearing());
            final Element<HearingBooking> reListedHearing = element(reListedHearingId, randomHearing());
            final Element<HearingBooking> adjournedHearing = element(hearingToBeAdjourned.getId(),
                hearingToBeAdjourned.getValue().toBuilder()
                    .status(HearingStatus.ADJOURNED_AND_RE_LISTED)
                    .cancellationReason(adjournmentReason.getReason())
                    .build());

            final Element<HearingFurtherEvidenceBundle> documentBundle = randomDocumentBundle(hearingToBeAdjourned);

            final Element<HearingFurtherEvidenceBundle> reListedHearingBundle = element(reListedHearingId,
                documentBundle.getValue().toBuilder()
                    .hearingName(reListedHearing.getValue().toLabel())
                    .build());

            final CaseData caseData = CaseData.builder()
                .hearingDetails(newArrayList(hearingToBeAdjourned, otherHearing))
                .hearingFurtherEvidenceDocuments(newArrayList(documentBundle))
                .adjournmentReason(adjournmentReason)
                .build();

            service.adjournAndReListHearing(caseData, hearingToBeAdjourned.getId(), reListedHearing.getValue());

            assertThat(caseData.getHearingDetails()).containsExactly(otherHearing, reListedHearing);
            assertThat(caseData.getCancelledHearingDetails()).containsExactly(adjournedHearing);
            assertThat(caseData.getHearingFurtherEvidenceDocuments()).containsExactly(reListedHearingBundle);
        }
    }

    @Nested
    class Vacating {

        @Test
        void shouldVacateHearingWithVacatedReason() {
            HearingCancellationReason vacatedReason = HearingCancellationReason.builder()
                .reason("Reason 1")
                .build();

            Element<HearingBooking> hearingElement1 = element(hearing(time.now().plusDays(1), time.now().plusDays(2)));
            Element<HearingBooking> hearingElement2 = element(hearing(time.now().plusDays(2), time.now().plusDays(3)));

            Element<HearingBooking> vacatedHearing = element(hearingElement1.getId(),
                hearingElement1.getValue().toBuilder()
                    .status(HearingStatus.VACATED)
                    .cancellationReason(vacatedReason.getReason())
                    .build());

            CaseData caseData = CaseData.builder()
                .hearingDetails(newArrayList(hearingElement1, hearingElement2))
                .vacatedReason(vacatedReason)
                .build();

            service.vacateHearing(caseData, hearingElement1.getId());

            assertThat(caseData.getHearingDetails()).containsExactly(hearingElement2);
            assertThat(caseData.getCancelledHearingDetails()).containsExactly(vacatedHearing);
        }

        @Test
        void shouldVacateHearingWithoutVacatedReason() {
            Element<HearingBooking> hearingElement1 = element(hearing(time.now().plusDays(1), time.now().plusDays(2)));
            Element<HearingBooking> hearingElement2 = element(hearing(time.now().plusDays(2), time.now().plusDays(3)));

            Element<HearingBooking> vacatedHearing = element(hearingElement1.getId(),
                hearingElement1.getValue().toBuilder()
                    .status(HearingStatus.VACATED)
                    .build());

            CaseData caseData = CaseData.builder()
                .hearingDetails(newArrayList(hearingElement1, hearingElement2))
                .build();

            service.vacateHearing(caseData, hearingElement1.getId());

            assertThat(caseData.getHearingDetails()).containsExactly(hearingElement2);
            assertThat(caseData.getCancelledHearingDetails()).containsExactly(vacatedHearing);
        }

        @Test
        void shouldVacateAndReListHearingWithoutDocumentReassignment() {
            final UUID reListedHearingId = randomUUID();

            when(identityService.generateId()).thenReturn(reListedHearingId);

            HearingCancellationReason vacatedReason = HearingCancellationReason.builder()
                .reason("Reason 1")
                .build();

            Element<HearingBooking> hearingToBeVacated = element(randomHearing());
            Element<HearingBooking> otherHearing = element(randomHearing());
            Element<HearingBooking> reListedHearing = element(reListedHearingId, randomHearing());
            Element<HearingBooking> expectedVacatedHearing = element(hearingToBeVacated.getId(),
                hearingToBeVacated.getValue().toBuilder()
                    .status(HearingStatus.VACATED_AND_RE_LISTED)
                    .cancellationReason(vacatedReason.getReason())
                    .build());

            CaseData caseData = CaseData.builder()
                .hearingDetails(newArrayList(hearingToBeVacated, otherHearing))
                .vacatedReason(vacatedReason)
                .build();

            service.vacateAndReListHearing(caseData, hearingToBeVacated.getId(), reListedHearing.getValue());

            assertThat(caseData.getHearingDetails()).containsExactly(otherHearing, reListedHearing);
            assertThat(caseData.getCancelledHearingDetails()).containsExactly(expectedVacatedHearing);
            assertThat(caseData.getHearingFurtherEvidenceDocuments()).isEmpty();
        }

        @Test
        void shouldVacateAndReListHearingWithDocumentReassignment() {
            final UUID reListedHearingId = randomUUID();

            when(identityService.generateId()).thenReturn(reListedHearingId);

            HearingCancellationReason vacatedReason = HearingCancellationReason.builder()
                .reason("Reason 1")
                .build();

            final Element<HearingBooking> hearingToBeVacated = element(randomHearing());
            final Element<HearingBooking> otherHearing = element(randomHearing());
            final Element<HearingBooking> reListedHearing = element(reListedHearingId, randomHearing());
            final Element<HearingBooking> vacatedHearing = element(hearingToBeVacated.getId(),
                hearingToBeVacated.getValue().toBuilder()
                    .status(HearingStatus.VACATED_AND_RE_LISTED)
                    .cancellationReason(vacatedReason.getReason())
                    .build());

            final Element<HearingFurtherEvidenceBundle> documentBundle = randomDocumentBundle(hearingToBeVacated);

            final Element<HearingFurtherEvidenceBundle> reListedHearingBundle = element(reListedHearingId,
                documentBundle.getValue().toBuilder()
                    .hearingName(reListedHearing.getValue().toLabel())
                    .build());

            final CaseData caseData = CaseData.builder()
                .hearingDetails(newArrayList(hearingToBeVacated, otherHearing))
                .hearingFurtherEvidenceDocuments(newArrayList(documentBundle))
                .vacatedReason(vacatedReason)
                .build();

            service.vacateAndReListHearing(caseData, hearingToBeVacated.getId(), reListedHearing.getValue());

            assertThat(caseData.getHearingDetails()).containsExactly(otherHearing, reListedHearing);
            assertThat(caseData.getCancelledHearingDetails()).containsExactly(vacatedHearing);
            assertThat(caseData.getHearingFurtherEvidenceDocuments()).containsExactly(reListedHearingBundle);
        }
    }

    @Nested
    class GetSelectedDynamicListType {

        @Test
        void shouldReturnFutureAndTodayHearingDateListWhenHearingOptionIsVacateHearing() {
            Element<HearingBooking> hearingToBeAdjourned = element(randomHearing());
            Element<HearingBooking> hearingToBeVacated = element(randomHearing());

            CaseData caseData = CaseData.builder()
                .hearingOption(VACATE_HEARING)
                .pastAndTodayHearingDateList(hearingToBeAdjourned)
                .futureAndTodayHearingDateList(hearingToBeVacated)
                .build();

            Object dynamicList = service.getSelectedDynamicListType(caseData);
            assertThat(dynamicList).isEqualTo(hearingToBeVacated);
        }

        @Test
        void shouldReturnPastAndTodayHearingDateListWhenHearingOptionIsAdjournedHearing() {
            Element<HearingBooking> hearingToBeAdjourned = element(randomHearing());
            Element<HearingBooking> hearingToBeVacated = element(randomHearing());

            CaseData caseData = CaseData.builder()
                .hearingOption(ADJOURN_HEARING)
                .pastAndTodayHearingDateList(hearingToBeAdjourned)
                .futureAndTodayHearingDateList(hearingToBeVacated)
                .build();

            Object dynamicList = service.getSelectedDynamicListType(caseData);
            assertThat(dynamicList).isEqualTo(hearingToBeAdjourned);
        }


        private HearingBooking randomHearing() {
            LocalDateTime startDate = LocalDateTime.now().minusDays(2);
            return HearingBooking.builder()
                .type(CASE_MANAGEMENT)
                .startDate(startDate)
                .endDate(startDate.plusDays(1))
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeTitle(HER_HONOUR_JUDGE)
                    .judgeLastName("Judy")
                    .build())
                .venue("96")
                .build();
        }
    }

    private Element<HearingFurtherEvidenceBundle> randomDocumentBundle(Element<HearingBooking> hearingBooking) {
        Element<SupportingEvidenceBundle> adjournedHearingDocument1 = element(SupportingEvidenceBundle.builder()
            .document(TestDataHelper.testDocumentReference())
            .name(randomAlphanumeric(10))
            .build());

        Element<SupportingEvidenceBundle> adjournedHearingDocument2 = element(SupportingEvidenceBundle.builder()
            .document(TestDataHelper.testDocumentReference())
            .name(randomAlphanumeric(10))
            .build());

        return element(hearingBooking.getId(),
            HearingFurtherEvidenceBundle.builder()
                .hearingName(hearingBooking.getValue().toLabel())
                .supportingEvidenceBundle(List.of(adjournedHearingDocument1, adjournedHearingDocument2))
                .build());
    }

    private static HearingBooking hearing(LocalDateTime start, LocalDateTime end) {
        return HearingBooking.builder()
            .startDate(start)
            .endDate(end)
            .venue(VENUE)
            .type(CASE_MANAGEMENT)
            .build();
    }

    private HearingBooking randomHearing() {
        LocalDateTime startDate = LocalDateTime.now().plusDays(nextLong(1, 100));
        return HearingBooking.builder()
            .startDate(startDate)
            .endDate(startDate.plusDays(nextLong(1, 5)))
            .venue(randomAlphanumeric(10))
            .additionalNotes(randomAlphanumeric(100))
            .type(CASE_MANAGEMENT)
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

}
