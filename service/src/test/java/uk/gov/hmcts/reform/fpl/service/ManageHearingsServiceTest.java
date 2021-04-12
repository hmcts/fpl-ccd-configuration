package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.HearingOptions;
import uk.gov.hmcts.reform.fpl.enums.HearingReListOption;
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
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfHearing;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.docmosis.NoticeOfHearingGenerationService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.time.LocalDate.now;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.NOTICE_OF_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.ADJOURN_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.EDIT_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.NEW_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.RE_LIST_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.VACATE_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.DRAFT_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingReListOption.NONE;
import static uk.gov.hmcts.reform.fpl.enums.HearingReListOption.RE_LIST_LATER;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.ADJOURNED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.ADJOURNED_AND_RE_LISTED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.ADJOURNED_TO_BE_RE_LISTED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.VACATED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.VACATED_AND_RE_LISTED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.VACATED_TO_BE_RE_LISTED;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.hearing.HearingPresence.IN_PERSON;
import static uk.gov.hmcts.reform.fpl.enums.hearing.HearingPresence.REMOTE;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.FUTURE_HEARING_LIST;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.HAS_EXISTING_HEARINGS_FLAG;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.HAS_FUTURE_HEARING_FLAG;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.HAS_HEARINGS_TO_ADJOURN;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.HAS_HEARINGS_TO_VACATE;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.HAS_HEARING_TO_RE_LIST;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.HEARING_DATE_LIST;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.PAST_HEARING_LIST;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.TO_RE_LIST_HEARING_LABEL;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.TO_RE_LIST_HEARING_LIST;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testJudge;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testJudgeAndLegalAdviser;

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

    public static final UUID RE_LISTED_HEARING_ID = randomUUID();
    public static final UUID LINKED_CMO_ID = randomUUID();
    public static final UUID HEARING_BUNDLE_ID = randomUUID();

    private final Time time = new FixedTimeConfiguration().stoppedTime();
    private final HearingVenueLookUpService hearingVenueLookUpService = mock(HearingVenueLookUpService.class);
    private final NoticeOfHearingGenerationService noticeOfHearingGenerationService = mock(
        NoticeOfHearingGenerationService.class
    );
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService = mock(
        DocmosisDocumentGeneratorService.class
    );
    private final UploadDocumentService uploadDocumentService = mock(UploadDocumentService.class);
    private final IdentityService identityService = mock(IdentityService.class);

    private final ManageHearingsService service = new ManageHearingsService(
        noticeOfHearingGenerationService, docmosisDocumentGeneratorService, uploadDocumentService,
        hearingVenueLookUpService, new ObjectMapper(), identityService, time
    );

    @Nested
    class PopulatePastAndFutureHearingListsTest {

        DynamicList emptyDynamicList = dynamicList();

        @Test
        void shouldPopulateHearingDataWhenPastAndFutureHearingsExist() {
            Element<HearingBooking> futureHearing1 = hearingFromToday(3);
            Element<HearingBooking> futureHearing2 = hearingFromToday(2);
            Element<HearingBooking> todayHearing = hearingFromToday(0);
            Element<HearingBooking> pastHearing1 = hearingFromToday(-2);
            Element<HearingBooking> pastHearing2 = hearingFromToday(-3);

            DynamicList expectedHearingList = dynamicList(futureHearing1, futureHearing2);
            DynamicList expectedPastHearingList = dynamicList(todayHearing, pastHearing1, pastHearing2);
            DynamicList expectedFutureHearingList = dynamicList(futureHearing1, futureHearing2, todayHearing);

            CaseData initialCaseData = CaseData.builder()
                .hearingDetails(List.of(futureHearing1, futureHearing2, todayHearing, pastHearing1, pastHearing2))
                .build();

            Map<String, Object> data = service.populateHearingLists(initialCaseData);

            assertThat(data)
                .containsEntry(HAS_HEARINGS_TO_ADJOURN, "Yes")
                .containsEntry(HAS_FUTURE_HEARING_FLAG, "Yes")
                .containsEntry(HAS_HEARINGS_TO_VACATE, "Yes")
                .containsEntry(HAS_EXISTING_HEARINGS_FLAG, "Yes")
                .containsEntry(HEARING_DATE_LIST, expectedHearingList)
                .containsEntry(PAST_HEARING_LIST, expectedPastHearingList)
                .containsEntry(FUTURE_HEARING_LIST, expectedFutureHearingList)
                .containsEntry(TO_RE_LIST_HEARING_LIST, emptyDynamicList)
                .doesNotContainKeys(HAS_HEARING_TO_RE_LIST)
                .doesNotContainKeys(TO_RE_LIST_HEARING_LABEL);
        }

        @Test
        void shouldPopulateHearingDataWhenOnlyCancelledHearingsPresent() {
            Element<HearingBooking> cancelledHearing1 = hearing(LocalDate.of(2020, 10, 1), ADJOURNED_TO_BE_RE_LISTED);
            Element<HearingBooking> cancelledHearing2 = hearing(LocalDate.of(2020, 9, 12), VACATED_TO_BE_RE_LISTED);
            Element<HearingBooking> cancelledHearing3 = hearing(now(), ADJOURNED_AND_RE_LISTED);
            Element<HearingBooking> cancelledHearing4 = hearing(now(), VACATED_AND_RE_LISTED);
            Element<HearingBooking> cancelledHearing5 = hearing(now(), ADJOURNED);
            Element<HearingBooking> cancelledHearing6 = hearing(now(), VACATED);

            CaseData initialCaseData = CaseData.builder()
                .cancelledHearingDetails(List.of(
                    cancelledHearing1,
                    cancelledHearing2,
                    cancelledHearing3,
                    cancelledHearing4,
                    cancelledHearing5,
                    cancelledHearing6))
                .build();

            Map<String, Object> data = service.populateHearingLists(initialCaseData);

            Object expectedHearingList = dynamicList(cancelledHearing1, cancelledHearing2);

            assertThat(data)
                .containsEntry(HAS_HEARING_TO_RE_LIST, "Yes")
                .containsEntry(TO_RE_LIST_HEARING_LABEL,
                    "Case management hearing, 1 October 2020 - adjourned\n"
                        + "Case management hearing, 12 September 2020 - vacated")
                .containsEntry(TO_RE_LIST_HEARING_LIST, expectedHearingList)
                .containsEntry(HAS_EXISTING_HEARINGS_FLAG, "Yes")
                .containsEntry(HEARING_DATE_LIST, emptyDynamicList)
                .containsEntry(PAST_HEARING_LIST, emptyDynamicList)
                .containsEntry(FUTURE_HEARING_LIST, emptyDynamicList)
                .doesNotContainKeys(HAS_HEARINGS_TO_ADJOURN)
                .doesNotContainKeys(HAS_FUTURE_HEARING_FLAG)
                .doesNotContainKeys(HAS_HEARINGS_TO_VACATE);
        }

        @Test
        void shouldOnlyPopulatePastHearingDateListWhenOnlyHearingsInThePastExist() {
            Element<HearingBooking> pastHearing1 = hearingFromToday(-2);
            Element<HearingBooking> pastHearing2 = hearingFromToday(-3);

            Object expectedPastHearingList = dynamicList(pastHearing1, pastHearing2);

            CaseData initialCaseData = CaseData.builder()
                .hearingDetails(List.of(pastHearing1, pastHearing2))
                .build();

            Map<String, Object> data = service.populateHearingLists(initialCaseData);

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

            Object expectedHearingList = dynamicList(futureHearing1, futureHearing2);
            Object expectedFutureHearingList = dynamicList(futureHearing1, futureHearing2);


            CaseData initialCaseData = CaseData.builder()
                .hearingDetails(List.of(futureHearing1, futureHearing2))
                .build();

            Map<String, Object> data = service.populateHearingLists(initialCaseData);

            assertThat(data)
                .containsEntry(HAS_FUTURE_HEARING_FLAG, "Yes")
                .containsEntry(HAS_HEARINGS_TO_VACATE, "Yes")
                .containsEntry(HAS_EXISTING_HEARINGS_FLAG, "Yes")
                .containsEntry(HEARING_DATE_LIST, expectedHearingList)
                .containsEntry(PAST_HEARING_LIST, emptyDynamicList)
                .containsEntry(FUTURE_HEARING_LIST, expectedFutureHearingList);
        }

        private Element<HearingBooking> hearingFromToday(int daysFromToday) {
            return hearing(now().plusDays(daysFromToday), null);
        }

        private Element<HearingBooking> hearing(LocalDate startDate, HearingStatus status) {
            final LocalDateTime startTime = startDate.atStartOfDay();
            return element(HearingBooking.builder()
                .status(status)
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

    @Nested
    class PreviousVenue {

        @Test
        void shouldReturnEmptyMapWhenNoHearingsAvailable() {
            CaseData caseData = CaseData.builder().build();

            assertThat(service.populatePreviousVenueFields(caseData)).isEmpty();
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
            .presence(REMOTE)
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
            "previousHearingVenue", previousHearingVenue,
            "hearingPresence", REMOTE
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
            .presence(IN_PERSON)
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
            "hearingVenueCustom", VENUE_CUSTOM_ADDRESS,
            "hearingPresence", IN_PERSON
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
            .hearingPresence(IN_PERSON)
            .hearingStartDate(startDate)
            .hearingEndDate(endDate)
            .judgeAndLegalAdvisor(testJudgeAndLegalAdviser())
            .noticeOfHearingNotes("notes")
            .build();

        HearingBooking hearingBooking = service.getCurrentHearingBooking(caseData);

        HearingBooking expectedHearingBooking = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .venue(VENUE)
            .presence(IN_PERSON)
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
            .hearingPresence(REMOTE)
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
            .presence(REMOTE)
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
    class PastHearings {

        @Test
        void shouldSetStartDateHearingFieldsWhenHearingStartDateIsInThePast() {
            LocalDateTime hearingStartDate = LocalDateTime.of(2010, 3, 15, 20, 20);
            LocalDateTime hearingEndDate = LocalDateTime.now().plusDays(3);

            Map<String, Object> startDateFields = service.populateFieldsWhenPastHearingDateAdded(hearingStartDate,
                hearingEndDate);

            Map<String, Object> extractedFields = Map.of(
                "showConfirmPastHearingDatesPage", "Yes",
                "startDateFlag", "Yes",
                "hearingStartDateLabel", "15 March 2010, 8:20pm");

            assertThat(startDateFields).isEqualTo(extractedFields);
        }

        @Test
        void shouldSetEndDateHearingFieldsWhenHearingEndDateIsInThePast() {
            LocalDateTime hearingStartDate = LocalDateTime.now().plusDays(3);
            LocalDateTime hearingEndDate = LocalDateTime.of(2010, 3, 15, 20, 20);

            Map<String, Object> startDateFields = service.populateFieldsWhenPastHearingDateAdded(hearingStartDate,
                hearingEndDate);

            Map<String, Object> extractedFields = Map.of(
                "showConfirmPastHearingDatesPage", "Yes",
                "endDateFlag", "Yes",
                "hearingEndDateLabel", "15 March 2010, 8:20pm");

            assertThat(startDateFields).isEqualTo(extractedFields);
        }

        @Test
        void shouldSetBothStartAndEndHearingFieldsWhenBothHearingDatesAreInThePast() {
            LocalDateTime hearingStartDate = LocalDateTime.of(2011, 4, 16, 20, 20);
            LocalDateTime hearingEndDate = LocalDateTime.of(2010, 3, 15, 20, 20);

            Map<String, Object> hearingDateFields = service.populateFieldsWhenPastHearingDateAdded(hearingStartDate,
                hearingEndDate);

            Map<String, Object> extractedFields = Map.of(
                "showConfirmPastHearingDatesPage", "Yes",
                "startDateFlag", "Yes",
                "hearingStartDateLabel", "16 April 2011, 8:20pm",
                "endDateFlag", "Yes",
                "hearingEndDateLabel", "15 March 2010, 8:20pm");

            assertThat(hearingDateFields).isEqualTo(extractedFields);
        }

        @Test
        void shouldHidePastHearingsDatesPageWhenDatesAreInFuture() {
            LocalDateTime hearingStartDate = LocalDateTime.now().plusDays(1);
            LocalDateTime hearingEndDate = hearingStartDate.plusHours(1);

            Map<String, Object> hearingDateFields = service.populateFieldsWhenPastHearingDateAdded(hearingStartDate,
                hearingEndDate);

            Map<String, Object> extractedFields = Map.of("showConfirmPastHearingDatesPage", "No");

            assertThat(hearingDateFields).isEqualTo(extractedFields);
        }
    }

    @Nested
    class HearingDates {
        @Test
        void shouldSetHearingStartDateToCorrectDateWhenIncorrectDateAdded() {
            LocalDateTime expectedHearingStartDate = LocalDateTime.of(2010, 3, 15, 20, 20);
            CaseData caseData = CaseData.builder()
                .hearingStartDate(LocalDateTime.of(2011, 4, 16, 20, 20))
                .hearingStartDateConfirmation(expectedHearingStartDate)
                .build();

            Map<String, Object> hearingDateFields = service.updateHearingDates(caseData);

            assertThat(hearingDateFields).containsEntry("hearingStartDate", expectedHearingStartDate);
        }

        @Test
        void shouldSetHearingEndDateToCorrectDateWhenIncorrectDateAdded() {
            LocalDateTime expectedHearingEndDate = LocalDateTime.of(2010, 3, 15, 20, 20);
            CaseData caseData = CaseData.builder()
                .hearingEndDate(LocalDateTime.of(2011, 4, 16, 20, 20))
                .hearingEndDateConfirmation(expectedHearingEndDate)
                .build();

            Map<String, Object> hearingDateFields = service.updateHearingDates(caseData);

            assertThat(hearingDateFields).containsEntry("hearingEndDate", expectedHearingEndDate);
        }

        @Test
        void shouldSetHearingStartAndEndDateToCorrectDateWhenIncorrectDatesAdded() {
            LocalDateTime expectedHearingEndDate = LocalDateTime.of(2010, 3, 15, 20, 20);
            LocalDateTime expectedHearingStartDate = LocalDateTime.of(2001, 4, 15, 20, 20);

            CaseData caseData = CaseData.builder()
                .hearingEndDate(LocalDateTime.of(2011, 4, 16, 20, 20))
                .hearingStartDate(LocalDateTime.of(2011, 6, 18, 20, 20))
                .hearingStartDateConfirmation(expectedHearingStartDate)
                .hearingEndDateConfirmation(expectedHearingEndDate)
                .build();

            Map<String, Object> hearingDateFields = service.updateHearingDates(caseData);

            Map<String, Object> extractedFields = Map.of("hearingEndDate", expectedHearingEndDate,
                "hearingStartDate", expectedHearingStartDate);

            assertThat(hearingDateFields).containsAllEntriesOf(extractedFields);
        }
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
        void shouldAdjournHearingToBeReleasedLater() {
            shouldAdjournHearing(RE_LIST_LATER, ADJOURNED_TO_BE_RE_LISTED);
        }

        @Test
        void shouldAdjournHearingWithoutReListing() {
            shouldAdjournHearing(NONE, ADJOURNED);
        }

        @Test
        void shouldAdjournAndReListHearingWithoutDocumentReassignment() {
            when(identityService.generateId()).thenReturn(RE_LISTED_HEARING_ID);

            HearingCancellationReason adjournmentReason = HearingCancellationReason.builder()
                .reason("Reason 1")
                .build();

            Element<HearingBooking> hearingToBeAdjourned = element(randomHearing());
            Element<HearingBooking> otherHearing = element(randomHearing());
            Element<HearingBooking> reListedHearing = element(RE_LISTED_HEARING_ID, randomHearing());
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
            when(identityService.generateId()).thenReturn(RE_LISTED_HEARING_ID);

            HearingCancellationReason adjournmentReason = HearingCancellationReason.builder()
                .reason("Reason 1")
                .build();

            final Element<HearingBooking> hearingToBeAdjourned = element(randomHearing());
            final Element<HearingBooking> otherHearing = element(randomHearing());
            final Element<HearingBooking> reListedHearing = element(RE_LISTED_HEARING_ID, randomHearing());
            final Element<HearingBooking> adjournedHearing = element(hearingToBeAdjourned.getId(),
                hearingToBeAdjourned.getValue().toBuilder()
                    .status(HearingStatus.ADJOURNED_AND_RE_LISTED)
                    .cancellationReason(adjournmentReason.getReason())
                    .build());

            final Element<HearingFurtherEvidenceBundle> documentBundle = randomDocumentBundle(hearingToBeAdjourned);

            final Element<HearingFurtherEvidenceBundle> reListedHearingBundle = element(RE_LISTED_HEARING_ID,
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

        void shouldAdjournHearing(HearingReListOption hearingReListOption, HearingStatus expectedStatus) {
            HearingCancellationReason adjournmentReason = HearingCancellationReason.builder()
                .reason("Reason 1")
                .build();

            Element<HearingBooking> hearingElement1 = element(hearing(time.now().plusDays(1), time.now().plusDays(2)));
            Element<HearingBooking> hearingElement2 = element(hearing(time.now().plusDays(2), time.now().plusDays(3)));

            Element<HearingBooking> adjournedHearing = element(hearingElement1.getId(),
                hearingElement1.getValue().toBuilder()
                    .status(expectedStatus)
                    .cancellationReason(adjournmentReason.getReason())
                    .build());

            final Element<HearingFurtherEvidenceBundle> adjournedHearingBundle = randomDocumentBundle(hearingElement1);

            CaseData caseData = CaseData.builder()
                .hearingDetails(newArrayList(hearingElement1, hearingElement2))
                .adjournmentReason(adjournmentReason)
                .hearingReListOption(hearingReListOption)
                .hearingFurtherEvidenceDocuments(List.of(adjournedHearingBundle))
                .build();

            final String documentBundleName = adjournedHearingBundle.getValue().getHearingName();
            final String updatedDocumentBundleName = String.format("%s - %s", documentBundleName, "adjourned");

            service.adjournHearing(caseData, hearingElement1.getId());

            assertThat(caseData.getHearingDetails()).containsExactly(hearingElement2);
            assertThat(caseData.getCancelledHearingDetails()).containsExactly(adjournedHearing);
            assertThat(adjournedHearingBundle.getValue().getHearingName()).isEqualTo(updatedDocumentBundleName);
        }

        @Test
        void shouldAdjournAndReListHearingAndUpdateDraftCaseManagementOrder() {
            when(identityService.generateId()).thenReturn(RE_LISTED_HEARING_ID);

            HearingCancellationReason adjournmentReason = HearingCancellationReason.builder()
                .reason("Reason 1")
                .build();

            Element<HearingBooking> hearingToBeAdjourned = element(randomHearingWithCMO(LINKED_CMO_ID));
            final Element<HearingBooking> otherHearing = element(randomHearing());
            final Element<HearingBooking> reListedHearing = element(RE_LISTED_HEARING_ID, randomHearing());
            final Element<HearingBooking> adjournedHearing = element(hearingToBeAdjourned.getId(),
                hearingToBeAdjourned.getValue().toBuilder()
                    .status(HearingStatus.ADJOURNED_AND_RE_LISTED)
                    .cancellationReason(adjournmentReason.getReason())
                    .caseManagementOrderId(LINKED_CMO_ID)
                    .build());

            final Element<HearingOrder> linkedDraftCMO = element(LINKED_CMO_ID,
                HearingOrder.builder().type(DRAFT_CMO).hearing(reListedHearing.getValue().toLabel()).build());

            final CaseData caseData = CaseData.builder()
                .hearingDetails(newArrayList(hearingToBeAdjourned, otherHearing))
                .adjournmentReason(adjournmentReason)
                .draftUploadedCMOs(newArrayList(linkedDraftCMO))
                .build();

            service.adjournAndReListHearing(caseData, hearingToBeAdjourned.getId(), reListedHearing.getValue());

            assertThat(caseData.getHearingDetails()).containsExactly(otherHearing, reListedHearing);
            assertThat(caseData.getCancelledHearingDetails()).containsExactly(adjournedHearing);
            assertThat(caseData.getDraftUploadedCMOs()).containsExactly(
                element(LINKED_CMO_ID, HearingOrder.builder()
                    .type(DRAFT_CMO)
                    .hearing(adjournedHearing.getValue().toLabel())
                    .build()));
        }

        @Test
        void shouldAdjournAndReListHearingAndUpdateHearingOrdersBundle() {
            when(identityService.generateId()).thenReturn(RE_LISTED_HEARING_ID);

            HearingCancellationReason adjournmentReason = HearingCancellationReason.builder()
                .reason("Reason 1")
                .build();

            final Element<HearingBooking> hearingToBeAdjourned = element(randomHearingWithCMO(LINKED_CMO_ID));
            final Element<HearingBooking> otherHearing = element(randomHearing());
            final Element<HearingBooking> reListedHearing = element(RE_LISTED_HEARING_ID, randomHearing());
            final Element<HearingBooking> adjournedHearing = element(hearingToBeAdjourned.getId(),
                hearingToBeAdjourned.getValue().toBuilder()
                    .status(HearingStatus.ADJOURNED_AND_RE_LISTED)
                    .cancellationReason(adjournmentReason.getReason())
                    .build());

            final Element<HearingOrder> linkedDraftCMO = element(LINKED_CMO_ID,
                HearingOrder.builder().type(DRAFT_CMO).status(DRAFT)
                    .hearing(reListedHearing.getValue().toLabel()).build());

            final CaseData caseData = CaseData.builder()
                .hearingDetails(newArrayList(hearingToBeAdjourned, otherHearing))
                .adjournmentReason(adjournmentReason)
                .hearingOrdersBundlesDrafts(List.of(element(HEARING_BUNDLE_ID,
                    HearingOrdersBundle.builder()
                        .orders(newArrayList(linkedDraftCMO))
                        .build())))
                .build();

            service.adjournAndReListHearing(caseData, hearingToBeAdjourned.getId(), reListedHearing.getValue());

            assertThat(caseData.getHearingDetails()).containsExactly(otherHearing, reListedHearing);
            assertThat(caseData.getCancelledHearingDetails()).containsExactly(adjournedHearing);
            assertThat(caseData.getHearingOrdersBundlesDrafts()).contains(
                element(HEARING_BUNDLE_ID, HearingOrdersBundle.builder()
                    .hearingName(adjournedHearing.getValue().toLabel())
                    .orders(newArrayList(element(linkedDraftCMO.getId(),
                        linkedDraftCMO.getValue().toBuilder()
                            .hearing(adjournedHearing.getValue().toLabel()).build())))
                    .build()));
        }
    }

    @Nested
    class Vacating {

        @Test
        void shouldVacateHearingToBeReListedLater() {
            shouldVacateHearing(RE_LIST_LATER, VACATED_TO_BE_RE_LISTED);
        }

        @Test
        void shouldVacateHearingWithoutReListing() {
            shouldVacateHearing(NONE, VACATED);
        }

        @Test
        void shouldVacateHearingWithVacatedReason() {
            HearingCancellationReason vacatedReason = HearingCancellationReason.builder()
                .reason("Reason 1")
                .build();

            Element<HearingBooking> hearingElement1 = element(hearing(time.now().plusDays(1), time.now().plusDays(2)));
            Element<HearingBooking> hearingElement2 = element(hearing(time.now().plusDays(2), time.now().plusDays(3)));

            Element<HearingBooking> vacatedHearing = element(hearingElement1.getId(),
                hearingElement1.getValue().toBuilder()
                    .status(VACATED)
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
                    .status(VACATED)
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
            when(identityService.generateId()).thenReturn(RE_LISTED_HEARING_ID);

            HearingCancellationReason vacatedReason = HearingCancellationReason.builder()
                .reason("Reason 1")
                .build();

            Element<HearingBooking> hearingToBeVacated = element(randomHearing());
            Element<HearingBooking> otherHearing = element(randomHearing());
            Element<HearingBooking> reListedHearing = element(RE_LISTED_HEARING_ID, randomHearing());
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
            when(identityService.generateId()).thenReturn(RE_LISTED_HEARING_ID);

            HearingCancellationReason vacatedReason = HearingCancellationReason.builder()
                .reason("Reason 1")
                .build();

            final Element<HearingBooking> hearingToBeVacated = element(randomHearing());
            final Element<HearingBooking> otherHearing = element(randomHearing());
            final Element<HearingBooking> reListedHearing = element(RE_LISTED_HEARING_ID, randomHearing());
            final Element<HearingBooking> vacatedHearing = element(hearingToBeVacated.getId(),
                hearingToBeVacated.getValue().toBuilder()
                    .status(HearingStatus.VACATED_AND_RE_LISTED)
                    .cancellationReason(vacatedReason.getReason())
                    .build());

            final Element<HearingFurtherEvidenceBundle> documentBundle = randomDocumentBundle(hearingToBeVacated);

            final Element<HearingFurtherEvidenceBundle> reListedHearingBundle = element(RE_LISTED_HEARING_ID,
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

        @Test
        void shouldVacateAndUpdateDraftCaseManagementOrder() {
            when(identityService.generateId()).thenReturn(RE_LISTED_HEARING_ID);

            HearingCancellationReason vacatedReason = HearingCancellationReason.builder()
                .reason("Reason 1")
                .build();

            final Element<HearingBooking> hearingToBeVacated = element(randomHearing().toBuilder()
                .caseManagementOrderId(LINKED_CMO_ID)
                .build());

            final Element<HearingBooking> otherHearing = element(randomHearing());
            final Element<HearingBooking> reListedHearing = element(RE_LISTED_HEARING_ID, randomHearing());
            final Element<HearingBooking> vacatedHearing = element(hearingToBeVacated.getId(),
                hearingToBeVacated.getValue().toBuilder()
                    .status(HearingStatus.VACATED_AND_RE_LISTED)
                    .cancellationReason(vacatedReason.getReason())
                    .build());

            final Element<HearingFurtherEvidenceBundle> documentBundle = randomDocumentBundle(hearingToBeVacated);

            final Element<HearingFurtherEvidenceBundle> reListedHearingBundle = element(RE_LISTED_HEARING_ID,
                documentBundle.getValue().toBuilder()
                    .hearingName(reListedHearing.getValue().toLabel())
                    .build());

            final Element<HearingOrder> linkedDraftCMO = element(LINKED_CMO_ID, HearingOrder.builder().build());

            final CaseData caseData = CaseData.builder()
                .hearingDetails(newArrayList(hearingToBeVacated, otherHearing))
                .hearingFurtherEvidenceDocuments(newArrayList(documentBundle))
                .vacatedReason(vacatedReason)
                .draftUploadedCMOs(List.of(linkedDraftCMO))
                .build();

            service.vacateAndReListHearing(caseData, hearingToBeVacated.getId(), reListedHearing.getValue());

            assertThat(caseData.getHearingDetails()).containsExactly(otherHearing, reListedHearing);
            assertThat(caseData.getCancelledHearingDetails()).containsExactly(vacatedHearing);
            assertThat(caseData.getHearingFurtherEvidenceDocuments()).containsExactly(reListedHearingBundle);
            assertThat(caseData.getDraftUploadedCMOs()).containsExactly(
                element(LINKED_CMO_ID, HearingOrder.builder().hearing(vacatedHearing.getValue().toLabel())
                    .build()));
        }

        @Test
        void shouldVacateHearingWhenTheHearingDoesNotHaveACaseManagementOrderId() {
            when(identityService.generateId()).thenReturn(RE_LISTED_HEARING_ID);

            HearingCancellationReason vacatedReason = HearingCancellationReason.builder()
                .reason("Reason 1")
                .build();

            final Element<HearingBooking> hearingToBeVacated = element(randomHearing().toBuilder()
                .caseManagementOrderId(null)
                .build());

            final Element<HearingBooking> reListedHearing = element(RE_LISTED_HEARING_ID, randomHearing());
            final Element<HearingBooking> vacatedHearing = element(hearingToBeVacated.getId(),
                hearingToBeVacated.getValue().toBuilder()
                    .status(HearingStatus.VACATED_AND_RE_LISTED)
                    .cancellationReason(vacatedReason.getReason())
                    .build());

            final Element<HearingFurtherEvidenceBundle> documentBundle = randomDocumentBundle(hearingToBeVacated);

            final Element<HearingFurtherEvidenceBundle> reListedHearingBundle = element(RE_LISTED_HEARING_ID,
                documentBundle.getValue().toBuilder()
                    .hearingName(reListedHearing.getValue().toLabel())
                    .build());

            final Element<HearingOrder> linkedDraftCMO = element(LINKED_CMO_ID, HearingOrder.builder().build());

            final CaseData caseData = CaseData.builder()
                .hearingDetails(newArrayList(hearingToBeVacated))
                .hearingFurtherEvidenceDocuments(newArrayList(documentBundle))
                .vacatedReason(vacatedReason)
                .draftUploadedCMOs(List.of(linkedDraftCMO))
                .build();

            service.vacateAndReListHearing(caseData, hearingToBeVacated.getId(), reListedHearing.getValue());

            assertThat(caseData.getHearingDetails()).containsExactly(reListedHearing);
            assertThat(caseData.getCancelledHearingDetails()).containsExactly(vacatedHearing);
            assertThat(caseData.getHearingFurtherEvidenceDocuments()).containsExactly(reListedHearingBundle);
            assertThat(caseData.getDraftUploadedCMOs()).containsExactly(linkedDraftCMO);
        }

        @Test
        void shouldVacateAndUpdateHearingOrdersBundleDrafts() {
            when(identityService.generateId()).thenReturn(RE_LISTED_HEARING_ID);

            HearingCancellationReason vacatedReason = HearingCancellationReason.builder()
                .reason("Reason 1")
                .build();

            final Element<HearingBooking> hearingToBeVacated = element(randomHearing().toBuilder()
                .caseManagementOrderId(LINKED_CMO_ID)
                .build());

            final Element<HearingBooking> otherHearing = element(randomHearing());
            final Element<HearingBooking> reListedHearing = element(RE_LISTED_HEARING_ID, randomHearing());
            final Element<HearingBooking> vacatedHearing = element(hearingToBeVacated.getId(),
                hearingToBeVacated.getValue().toBuilder()
                    .status(HearingStatus.VACATED_AND_RE_LISTED)
                    .cancellationReason(vacatedReason.getReason())
                    .build());

            final Element<HearingFurtherEvidenceBundle> documentBundle = randomDocumentBundle(hearingToBeVacated);

            final Element<HearingFurtherEvidenceBundle> reListedHearingBundle = element(RE_LISTED_HEARING_ID,
                documentBundle.getValue().toBuilder()
                    .hearingName(reListedHearing.getValue().toLabel())
                    .build());

            final Element<HearingOrder> linkedDraftCMO = element(LINKED_CMO_ID, HearingOrder.builder()
                .type(DRAFT_CMO).status(DRAFT)
                .build());

            final CaseData caseData = CaseData.builder()
                .hearingDetails(newArrayList(hearingToBeVacated, otherHearing))
                .hearingFurtherEvidenceDocuments(newArrayList(documentBundle))
                .vacatedReason(vacatedReason)
                .hearingOrdersBundlesDrafts(newArrayList(
                    element(HEARING_BUNDLE_ID, HearingOrdersBundle.builder()
                        .orders(newArrayList(linkedDraftCMO))
                        .build())
                ))
                .build();

            service.vacateAndReListHearing(caseData, hearingToBeVacated.getId(), reListedHearing.getValue());

            assertThat(caseData.getHearingDetails()).containsExactly(otherHearing, reListedHearing);
            assertThat(caseData.getCancelledHearingDetails()).containsExactly(vacatedHearing);
            assertThat(caseData.getHearingFurtherEvidenceDocuments()).containsExactly(reListedHearingBundle);
            assertThat(caseData.getHearingOrdersBundlesDrafts()).contains(
                element(HEARING_BUNDLE_ID, HearingOrdersBundle.builder()
                    .hearingName(vacatedHearing.getValue().toLabel())
                    .orders(newArrayList(element(linkedDraftCMO.getId(),
                        linkedDraftCMO.getValue().toBuilder().hearing(vacatedHearing.getValue().toLabel()).build())))
                    .build()));
        }

        void shouldVacateHearing(HearingReListOption reListOption, HearingStatus expectedStatus) {

            HearingCancellationReason vacatedReason = HearingCancellationReason.builder()
                .reason("Reason 1")
                .build();

            Element<HearingBooking> hearingElement1 = element(hearing(time.now().plusDays(1), time.now().plusDays(2)));
            Element<HearingBooking> hearingElement2 = element(hearing(time.now().plusDays(2), time.now().plusDays(3)));

            Element<HearingBooking> vacatedHearing = element(hearingElement1.getId(),
                hearingElement1.getValue().toBuilder()
                    .status(expectedStatus)
                    .cancellationReason(vacatedReason.getReason())
                    .build());

            final Element<HearingFurtherEvidenceBundle> vacatedHearingBundle = randomDocumentBundle(hearingElement1);

            CaseData caseData = CaseData.builder()
                .hearingDetails(newArrayList(hearingElement1, hearingElement2))
                .hearingReListOption(reListOption)
                .vacatedReason(vacatedReason)
                .hearingFurtherEvidenceDocuments(List.of(vacatedHearingBundle))
                .build();

            final String documentBundleName = vacatedHearingBundle.getValue().getHearingName();
            final String updatedDocumentBundleName = String.format("%s - %s", documentBundleName, "vacated");

            service.vacateHearing(caseData, hearingElement1.getId());

            assertThat(caseData.getHearingDetails()).containsExactly(hearingElement2);
            assertThat(caseData.getCancelledHearingDetails()).containsExactly(vacatedHearing);
            assertThat(vacatedHearingBundle.getValue().getHearingName()).isEqualTo(updatedDocumentBundleName);
        }
    }

    @Nested
    class ReListHearing {

        @Test
        void shouldReListAdjournedHearing() {
            Element<HearingBooking> adjournedHearing = element(randomHearing(ADJOURNED_TO_BE_RE_LISTED));
            Element<HearingBooking> vacatedHearing = element(randomHearing(VACATED_TO_BE_RE_LISTED));

            HearingBooking reListedHearing = randomHearing();

            Element<HearingBooking> expectedAdjournedHearing = element(
                adjournedHearing.getId(),
                adjournedHearing.getValue().toBuilder().status(HearingStatus.ADJOURNED_AND_RE_LISTED).build());

            CaseData caseData = CaseData.builder()
                .cancelledHearingDetails(newArrayList(adjournedHearing, vacatedHearing))
                .build();

            service.reListHearing(caseData, adjournedHearing.getId(), reListedHearing);

            assertThat(caseData.getHearingDetails()).extracting(Element::getValue).containsExactly(reListedHearing);
            assertThat(caseData.getCancelledHearingDetails()).containsExactly(expectedAdjournedHearing, vacatedHearing);
        }

        @Test
        void shouldReListVacatedHearing() {
            Element<HearingBooking> adjournedHearing = element(randomHearing(ADJOURNED_TO_BE_RE_LISTED));
            Element<HearingBooking> vacatedHearing = element(randomHearing(VACATED_TO_BE_RE_LISTED));

            HearingBooking reListedHearing = randomHearing();

            Element<HearingBooking> expectedVacatedHearing = element(vacatedHearing.getId(),
                vacatedHearing.getValue().toBuilder().status(HearingStatus.VACATED_AND_RE_LISTED).build());

            CaseData caseData = CaseData.builder()
                .cancelledHearingDetails(newArrayList(adjournedHearing, vacatedHearing))
                .build();

            service.reListHearing(caseData, vacatedHearing.getId(), reListedHearing);

            assertThat(caseData.getHearingDetails()).extracting(Element::getValue).containsExactly(reListedHearing);
            assertThat(caseData.getCancelledHearingDetails()).containsExactly(adjournedHearing, expectedVacatedHearing);
        }

        @Test
        void shouldReassignDocumentFromCancelledToReListedHearing() {
            when(identityService.generateId()).thenReturn(RE_LISTED_HEARING_ID);

            final Element<HearingBooking> adjournedHearing = element(randomHearing(ADJOURNED_TO_BE_RE_LISTED));
            final HearingBooking reListedHearing = randomHearing();

            final Element<HearingFurtherEvidenceBundle> adjournedHearingBundle = randomDocumentBundle(adjournedHearing);
            final Element<HearingFurtherEvidenceBundle> reListedHearingBundle = element(RE_LISTED_HEARING_ID,
                adjournedHearingBundle.getValue().toBuilder()
                    .hearingName(reListedHearing.toLabel())
                    .build());

            final CaseData caseData = CaseData.builder()
                .cancelledHearingDetails(newArrayList(adjournedHearing))
                .hearingFurtherEvidenceDocuments(newArrayList(adjournedHearingBundle))
                .build();

            service.reListHearing(caseData, adjournedHearing.getId(), reListedHearing);

            assertThat(caseData.getHearingFurtherEvidenceDocuments()).containsExactly(reListedHearingBundle);
        }

    }

    @Nested
    class HearingsDynamicList {

        @Test
        void shouldReturnFutureAndTodayHearingDateListWhenHearingOptionIsVacateHearing() {
            CaseData caseData = caseData(VACATE_HEARING);

            Object dynamicList = service.getHearingsDynamicList(caseData);
            assertThat(dynamicList).isEqualTo(caseData.getFutureAndTodayHearingDateList());
        }

        @Test
        void shouldReturnPastAndTodayHearingDateListWhenHearingOptionIsAdjournHearing() {
            CaseData caseData = caseData(ADJOURN_HEARING);

            Object dynamicList = service.getHearingsDynamicList(caseData);
            assertThat(dynamicList).isEqualTo(caseData.getPastAndTodayHearingDateList());
        }

        @Test
        void shouldReturnFutureHearingsWhenHearingOptionIsEdit() {
            CaseData caseData = caseData(EDIT_HEARING);

            Object dynamicList = service.getHearingsDynamicList(caseData);
            assertThat(dynamicList).isEqualTo(caseData.getHearingDateList());
        }

        @Test
        void shouldReturnHearingsToBeReListedWhenHearingOptionIsReList() {
            CaseData caseData = caseData(RE_LIST_HEARING);

            Object dynamicList = service.getHearingsDynamicList(caseData);
            assertThat(dynamicList).isEqualTo(caseData.getToReListHearingDateList());
        }

        @Test
        void shouldReturnNullWhenHearingOptionIsAddNew() {
            CaseData caseData = caseData(NEW_HEARING);

            Object dynamicList = service.getHearingsDynamicList(caseData);
            assertThat(dynamicList).isNull();
        }

        private CaseData caseData(HearingOptions hearingOptions) {
            return CaseData.builder()
                .hearingOption(hearingOptions)
                .pastAndTodayHearingDateList(randomDynamicList())
                .futureAndTodayHearingDateList(randomDynamicList())
                .toReListHearingDateList(randomDynamicList())
                .hearingDateList(randomDynamicList())
                .build();
        }

        private DynamicList randomDynamicList() {
            LocalDateTime startDate = LocalDateTime.now().minusDays(nextInt());

            HearingBooking randomHearing = HearingBooking.builder()
                .type(CASE_MANAGEMENT)
                .startDate(startDate)
                .endDate(startDate.plusDays(nextInt()))
                .build();

            return asDynamicList(wrapElements(randomHearing), HearingBooking::toLabel);
        }
    }

    @Nested
    class SelectedHearing {

        final UUID selectedPastAndTodayHearing = randomUUID();
        final UUID selectedFutureOrTodayHearing = randomUUID();
        final UUID selectedToReListHearing = randomUUID();
        final UUID selectedHearing = randomUUID();

        @Test
        void shouldReturnSelectedHearingIdWhenHearingOptionIsVacateHearing() {
            CaseData caseData = caseData(VACATE_HEARING);

            UUID selectedHearingId = service.getSelectedHearingId(caseData);
            assertThat(selectedHearingId).isEqualTo(selectedFutureOrTodayHearing);
        }

        @Test
        void shouldReturnSelectedHearingIdWhenHearingOptionIsAdjournHearing() {
            CaseData caseData = caseData(ADJOURN_HEARING);

            UUID selectedHearingId = service.getSelectedHearingId(caseData);
            assertThat(selectedHearingId).isEqualTo(selectedPastAndTodayHearing);
        }

        @Test
        void shouldReturnSelectedHearingIdWhenHearingOptionIsEdit() {
            CaseData caseData = caseData(EDIT_HEARING);

            UUID selectedHearingId = service.getSelectedHearingId(caseData);
            assertThat(selectedHearingId).isEqualTo(selectedHearing);
        }

        @Test
        void shouldReturnSelectedHearingIdWhenHearingOptionIsReList() {
            CaseData caseData = caseData(RE_LIST_HEARING);

            UUID selectedHearingId = service.getSelectedHearingId(caseData);
            assertThat(selectedHearingId).isEqualTo(selectedToReListHearing);
        }

        @Test
        void shouldReturnNullWhenHearingOptionIsAddNew() {
            CaseData caseData = caseData(NEW_HEARING);

            UUID selectedHearingId = service.getSelectedHearingId(caseData);
            assertThat(selectedHearingId).isNull();
        }

        private CaseData caseData(HearingOptions hearingOptions) {
            return CaseData.builder()
                .hearingOption(hearingOptions)
                .pastAndTodayHearingDateList(randomDynamicList(selectedPastAndTodayHearing))
                .futureAndTodayHearingDateList(randomDynamicList(selectedFutureOrTodayHearing))
                .toReListHearingDateList(randomDynamicList(selectedToReListHearing))
                .hearingDateList(randomDynamicList(selectedHearing))
                .build();
        }

        private DynamicList randomDynamicList(UUID selectedId) {
            LocalDateTime startDate = LocalDateTime.now().minusDays(nextInt());

            Element<HearingBooking> randomHearing1 = element(selectedId, HearingBooking.builder()
                .type(CASE_MANAGEMENT)
                .startDate(startDate)
                .endDate(startDate.plusDays(nextInt()))
                .build());

            Element<HearingBooking> randomHearing2 = element(selectedId, HearingBooking.builder()
                .type(CASE_MANAGEMENT)
                .startDate(startDate.plusDays(1))
                .endDate(startDate.plusDays(1).plusDays(nextInt()))
                .build());

            return asDynamicList(List.of(randomHearing1, randomHearing2), selectedId, HearingBooking::toLabel);
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
        return randomHearing(null);
    }

    private HearingBooking randomHearing(HearingStatus status) {
        return randomHearing(status, null);
    }

    private HearingBooking randomHearing(HearingStatus status, UUID cmoId) {
        LocalDateTime startDate = LocalDateTime.now().plusDays(nextLong(1, 100));
        return HearingBooking.builder()
            .status(status)
            .startDate(startDate)
            .endDate(startDate.plusDays(nextLong(1, 5)))
            .venue(randomAlphanumeric(10))
            .additionalNotes(randomAlphanumeric(100))
            .type(CASE_MANAGEMENT)
            .caseManagementOrderId(cmoId)
            .build();
    }

    private HearingBooking randomHearingWithCMO(UUID cmoId) {
        return randomHearing(null, cmoId);
    }

    private HearingBooking hearingWithCustomAddress(LocalDateTime start, LocalDateTime end) {
        return HearingBooking.builder()
            .startDate(start)
            .endDate(end)
            .venue("OTHER")
            .venueCustomAddress(VENUE_CUSTOM_ADDRESS)
            .build();
    }

    @SafeVarargs
    private static DynamicList dynamicList(Element<HearingBooking>... hearings) {
        return asDynamicList(Arrays.asList(hearings), HearingBooking::toLabel);
    }

}
