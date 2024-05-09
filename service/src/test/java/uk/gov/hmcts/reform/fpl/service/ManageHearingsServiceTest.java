package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.enums.HearingHousekeepReason;
import uk.gov.hmcts.reform.fpl.enums.HearingOptions;
import uk.gov.hmcts.reform.fpl.enums.HearingReListOption;
import uk.gov.hmcts.reform.fpl.enums.HearingStatus;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.exceptions.NoHearingBookingException;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingCancellationReason;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.ManageHearingHousekeepEventData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.PreviousHearingVenue;
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
import uk.gov.hmcts.reform.fpl.service.others.OthersNotifiedGenerator;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.NOTICE_OF_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingDuration.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.enums.HearingDuration.DAYS;
import static uk.gov.hmcts.reform.fpl.enums.HearingDuration.HOURS_MINS;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.ADJOURN_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.EDIT_FUTURE_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.EDIT_PAST_HEARING;
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
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FACT_FINDING;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.ENGLISH_TO_WELSH;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.enums.hearing.HearingAttendance.IN_PERSON;
import static uk.gov.hmcts.reform.fpl.enums.hearing.HearingAttendance.PHONE;
import static uk.gov.hmcts.reform.fpl.enums.hearing.HearingAttendance.VIDEO;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.FUTURE_HEARING_LIST;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.HAS_EXISTING_HEARINGS_FLAG;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.HAS_FUTURE_HEARINGS;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.HAS_HEARINGS_TO_ADJOURN;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.HAS_HEARING_TO_RE_LIST;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.HAS_PAST_HEARINGS;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.PAST_AND_TODAY_HEARING_DATE_LIST;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.PAST_HEARING_DATE_LIST;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.TO_RE_LIST_HEARING_LABEL;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.TO_RE_LIST_HEARING_LIST;
import static uk.gov.hmcts.reform.fpl.service.ManageHearingsService.VACATE_HEARING_LIST;
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

    private static final List<Element<Other>> SELECTED_OTHERS = wrapElements(Other.builder().build());

    private static final Document DOCUMENT = testDocument();

    private static final UUID RE_LISTED_HEARING_ID = randomUUID();
    private static final UUID LINKED_CMO_ID = randomUUID();
    private static final UUID HEARING_BUNDLE_ID = randomUUID();
    private static final LanguageTranslationRequirement TRANSLATION_REQUIREMENTS = ENGLISH_TO_WELSH;

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
    private final OthersService othersService = mock(OthersService.class);
    private final OthersNotifiedGenerator othersNotifiedGenerator = mock(OthersNotifiedGenerator.class);

    private final ManageHearingsService service = new ManageHearingsService(
        noticeOfHearingGenerationService, docmosisDocumentGeneratorService, uploadDocumentService,
        hearingVenueLookUpService, othersService, othersNotifiedGenerator, new ObjectMapper(), identityService, time
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

            DynamicList expectedPastHearingList = dynamicList(todayHearing, pastHearing1, pastHearing2);
            DynamicList expectedFutureHearingList = dynamicList(futureHearing1, futureHearing2);
            DynamicList expectedPastAndFutureHearingList = dynamicList(futureHearing1, futureHearing2, todayHearing,
                pastHearing1, pastHearing2);

            CaseData initialCaseData = CaseData.builder()
                .hearingDetails(List.of(futureHearing1, futureHearing2, todayHearing, pastHearing1, pastHearing2))
                .build();

            Map<String, Object> data = service.populateHearingLists(initialCaseData);

            assertThat(data)
                .containsEntry(HAS_HEARINGS_TO_ADJOURN, "Yes")
                .containsEntry(HAS_PAST_HEARINGS, "Yes")
                .containsEntry(HAS_FUTURE_HEARINGS, "Yes")
                .containsEntry(HAS_EXISTING_HEARINGS_FLAG, "Yes")
                .containsEntry(PAST_HEARING_DATE_LIST, expectedPastHearingList)
                .containsEntry(FUTURE_HEARING_LIST, expectedFutureHearingList)
                .containsEntry(PAST_AND_TODAY_HEARING_DATE_LIST, expectedPastHearingList)
                .containsEntry(VACATE_HEARING_LIST, expectedPastAndFutureHearingList)
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
                .containsEntry(PAST_HEARING_DATE_LIST, emptyDynamicList)
                .containsEntry(PAST_AND_TODAY_HEARING_DATE_LIST, emptyDynamicList)
                .containsEntry(VACATE_HEARING_LIST, emptyDynamicList)
                .doesNotContainKeys(HAS_HEARINGS_TO_ADJOURN)
                .doesNotContainKeys(HAS_PAST_HEARINGS);
        }

        @Test
        void shouldOnlyPopulatePastHearingDateListAndHearingDateListWhenOnlyHearingsInThePastExist() {
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
                .containsEntry(PAST_HEARING_DATE_LIST, expectedPastHearingList)
                .containsEntry(PAST_AND_TODAY_HEARING_DATE_LIST, expectedPastHearingList);
        }

        @Test
        void shouldOnlyPopulateFutureHearingDateListWhenOnlyHearingsInTheFutureExist() {
            Element<HearingBooking> futureHearing1 = hearingFromToday(3);
            Element<HearingBooking> futureHearing2 = hearingFromToday(2);

            Object expectedHearingList = dynamicList(futureHearing1, futureHearing2);

            CaseData initialCaseData = CaseData.builder()
                .hearingDetails(List.of(futureHearing1, futureHearing2))
                .build();

            Map<String, Object> data = service.populateHearingLists(initialCaseData);

            assertThat(data)
                .containsEntry(HAS_FUTURE_HEARINGS, "Yes")
                .containsEntry(HAS_EXISTING_HEARINGS_FLAG, "Yes")
                .containsEntry(PAST_HEARING_DATE_LIST, emptyDynamicList)
                .containsEntry(FUTURE_HEARING_LIST, expectedHearingList)
                .containsEntry(PAST_AND_TODAY_HEARING_DATE_LIST, emptyDynamicList)
                .containsEntry(VACATE_HEARING_LIST, expectedHearingList);
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
    class ClearPopulatedHearingFields {

        @Test
        void shouldResetPopulatedHearingCaseFields() {
            assertThat(service.clearPopulatedHearingFields())
                .containsEntry("hearingTypeDetails", null)
                .containsEntry("hearingType", null)
                .containsEntry("hearingTypeReason", null)
                .containsEntry("hearingStartDate", null)
                .containsEntry("hearingEndDate", null)
                .containsEntry("judgeAndLegalAdvisor", null)
                .containsEntry("hearingAttendance", List.of())
                .containsEntry("hearingAttendanceDetails", null)
                .containsEntry("preHearingAttendanceDetails", null)
                .containsEntry("sendNoticeOfHearingTranslationRequirements", null)
                .containsEntry("hearingDuration", null)
                .containsEntry("hearingDays", null)
                .containsEntry("hearingMinutes", null)
                .containsEntry("hearingHours", null)
                .containsEntry("hearingEndDateTime", null)
                .containsEntry("previousHearingVenue", null)
                .containsEntry("hearingVenue", null)
                .containsEntry("hearingVenueCustom", null);
        }

    }

    @Nested
    class NewHearingInitiation {

        @Test
        void shouldReturnEmptyMapWhenNoHearingsAvailable() {
            CaseData caseData = CaseData.builder().build();

            assertThat(service.initiateNewHearing(caseData)).isEmpty();
        }

        @Test
        void shouldPullCustomAddressFromHearingWhenHearingVenueIsOther() {
            given(hearingVenueLookUpService.getHearingVenue(any(HearingBooking.class))).willCallRealMethod();
            given(hearingVenueLookUpService.buildHearingVenue(any())).willCallRealMethod();

            CaseData caseData = CaseData.builder()
                .hearingDetails(List.of(element(hearingWithCustomAddress(
                    time.now().plusHours(1), time.now().plusHours(2)))))
                .build();

            Map<String, Object> previousVenueFields = service.initiateNewHearing(caseData);

            PreviousHearingVenue hearingVenue = PreviousHearingVenue.builder()
                .previousVenue("custom, address")
                .newVenueCustomAddress(VENUE_CUSTOM_ADDRESS)
                .build();

            assertThat(previousVenueFields).containsOnly(
                entry("previousHearingVenue", hearingVenue),
                entry("preHearingAttendanceDetails", "1 hour before the hearing"));
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

            Map<String, Object> previousVenueFields = service.initiateNewHearing(caseData);

            PreviousHearingVenue hearingVenue = PreviousHearingVenue.builder()
                .previousVenue(venueAddress)
                .build();

            assertThat(previousVenueFields).containsOnly(
                entry("previousHearingVenue", hearingVenue),
                entry("preHearingAttendanceDetails", "1 hour before the hearing"));
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
            .typeReason("Reason")
            .venue("OTHER")
            .venueCustomAddress(VENUE_CUSTOM_ADDRESS)
            .attendance(List.of(IN_PERSON, VIDEO))
            .attendanceDetails("Test attendance details")
            .preAttendanceDetails("Test pre attendance details")
            .startDate(startDate)
            .endDate(endDate)
            .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
            .previousHearingVenue(previousHearingVenue)
            .translationRequirements(TRANSLATION_REQUIREMENTS)
            .build();

        Map<String, Object> hearingCaseFields = service.populateHearingCaseFields(hearing, null);

        assertThat(hearingCaseFields).containsExactlyInAnyOrderEntriesOf(Map.ofEntries(
            Map.entry("hearingType", CASE_MANAGEMENT),
            Map.entry("hearingTypeReason", "Reason"),
            Map.entry("hearingStartDate", startDate),
            Map.entry("hearingEndDate", endDate),
            Map.entry("judgeAndLegalAdvisor", judgeAndLegalAdvisor),
            Map.entry("previousHearingVenue", previousHearingVenue),
            Map.entry("hearingAttendance", List.of(IN_PERSON, VIDEO)),
            Map.entry("hearingAttendanceDetails", "Test attendance details"),
            Map.entry("preHearingAttendanceDetails", "Test pre attendance details"),
            Map.entry("sendNoticeOfHearingTranslationRequirements", TRANSLATION_REQUIREMENTS),
            Map.entry("hearingDuration", "DATE_TIME"),
            Map.entry("hearingEndDateTime", endDate),
            Map.entry("hearingVenue", "OTHER"),
            Map.entry("hearingVenueCustom", VENUE_CUSTOM_ADDRESS)
        ));
    }

    @Test
    void shouldUnwrapHearingWhenNoPreviousVenueAndCustomHearingTypeUsedAndAllocatedJudgeUsed() {
        LocalDateTime startDate = time.now().plusDays(1);
        LocalDateTime endDate = time.now().plusHours(25);
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = testJudgeAndLegalAdviser();
        Judge allocatedJudge = testJudge();

        HearingBooking hearing = HearingBooking.builder()
            .type(FACT_FINDING)
            .typeDetails("Fact finding")
            .typeReason("Reason")
            .venue("OTHER")
            .venueCustomAddress(VENUE_CUSTOM_ADDRESS)
            .attendance(List.of(IN_PERSON))
            .attendanceDetails("Attendance details")
            .preAttendanceDetails("Pre attendance details")
            .startDate(startDate)
            .endDate(endDate)
            .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
            .previousHearingVenue(PreviousHearingVenue.builder().build())
            .translationRequirements(TRANSLATION_REQUIREMENTS)
            .build();

        Map<String, Object> hearingCaseFields = service.populateHearingCaseFields(hearing, allocatedJudge);

        assertThat(hearingCaseFields).containsExactlyInAnyOrderEntriesOf(Map.ofEntries(
            Map.entry("hearingType", FACT_FINDING),
            Map.entry("hearingTypeDetails", "Fact finding"),
            Map.entry("hearingTypeReason", "Reason"),
            Map.entry("hearingStartDate", startDate),
            Map.entry("hearingEndDate", endDate),
            Map.entry("judgeAndLegalAdvisor", judgeAndLegalAdvisor),
            Map.entry("hearingVenue", "OTHER"),
            Map.entry("hearingVenueCustom", VENUE_CUSTOM_ADDRESS),
            Map.entry("hearingAttendance", List.of(IN_PERSON)),
            Map.entry("hearingAttendanceDetails", "Attendance details"),
            Map.entry("preHearingAttendanceDetails", "Pre attendance details"),
            Map.entry("sendNoticeOfHearingTranslationRequirements", TRANSLATION_REQUIREMENTS),
            Map.entry("hearingDuration", "DATE_TIME"),
            Map.entry("hearingEndDateTime", endDate)
        ));

    }

    @Test
    void shouldSetHearingDayAndHearingDurationWhenHearingDaysIsSet() {
        int days = 9;
        LocalDateTime startDate = LocalDateTime.of(2022, 12, 5, 0, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2022, 12, 15, 0, 0, 0);
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = testJudgeAndLegalAdviser();
        Judge allocatedJudge = testJudge();

        HearingBooking hearing = HearingBooking.builder()
            .type(FACT_FINDING)
            .typeDetails("Fact finding")
            .venue("OTHER")
            .typeReason("Reason")
            .venueCustomAddress(VENUE_CUSTOM_ADDRESS)
            .attendance(List.of(IN_PERSON))
            .attendanceDetails("Attendance details")
            .preAttendanceDetails("Pre attendance details")
            .startDate(startDate)
            .endDate(endDate)
            .endDateDerived(YES.getValue())
            .hearingDays(days)
            .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
            .previousHearingVenue(PreviousHearingVenue.builder().build())
            .translationRequirements(TRANSLATION_REQUIREMENTS)
            .build();

        Map<String, Object> hearingCaseFields = service.populateHearingCaseFields(hearing, allocatedJudge);

        assertThat(hearingCaseFields).containsExactlyInAnyOrderEntriesOf(Map.ofEntries(
            Map.entry("hearingType", FACT_FINDING),
            Map.entry("hearingTypeDetails", "Fact finding"),
            Map.entry("hearingStartDate", startDate),
            Map.entry("hearingEndDate", endDate),
            Map.entry("judgeAndLegalAdvisor", judgeAndLegalAdvisor),
            Map.entry("hearingVenue", "OTHER"),
            Map.entry("hearingVenueCustom", VENUE_CUSTOM_ADDRESS),
            Map.entry("hearingAttendance", List.of(IN_PERSON)),
            Map.entry("hearingAttendanceDetails", "Attendance details"),
            Map.entry("preHearingAttendanceDetails", "Pre attendance details"),
            Map.entry("sendNoticeOfHearingTranslationRequirements", TRANSLATION_REQUIREMENTS),
            Map.entry("hearingDuration", DAYS.getType()),
            Map.entry("hearingDays", days),
            Map.entry("hearingTypeReason", "Reason")
        ));
    }

    @Test
    void shouldSetHearingHoursAndMinutesWhenHearingHoursAndMinutesIsSet() {
        int hours = 9;
        int minutes = 30;
        LocalDateTime startDate = time.now().plusDays(1);
        LocalDateTime endDate = startDate.plusHours(hours)
            .plusMinutes(minutes);
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = testJudgeAndLegalAdviser();
        Judge allocatedJudge = testJudge();

        HearingBooking hearing = HearingBooking.builder()
            .type(FACT_FINDING)
            .typeDetails("Fact finding")
            .venue("OTHER")
            .typeReason("Reason")
            .venueCustomAddress(VENUE_CUSTOM_ADDRESS)
            .attendance(List.of(IN_PERSON))
            .attendanceDetails("Attendance details")
            .preAttendanceDetails("Pre attendance details")
            .startDate(startDate)
            .endDate(endDate)
            .endDateDerived(YES.getValue())
            .hearingHours(hours)
            .hearingMinutes(minutes)
            .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
            .previousHearingVenue(PreviousHearingVenue.builder().build())
            .translationRequirements(TRANSLATION_REQUIREMENTS)
            .build();

        Map<String, Object> hearingCaseFields = service.populateHearingCaseFields(hearing, allocatedJudge);

        assertThat(hearingCaseFields).containsExactlyInAnyOrderEntriesOf(Map.ofEntries(
            Map.entry("hearingType", FACT_FINDING),
            Map.entry("hearingTypeDetails", "Fact finding"),
            Map.entry("hearingStartDate", startDate),
            Map.entry("hearingEndDate", endDate),
            Map.entry("judgeAndLegalAdvisor", judgeAndLegalAdvisor),
            Map.entry("hearingVenue", "OTHER"),
            Map.entry("hearingVenueCustom", VENUE_CUSTOM_ADDRESS),
            Map.entry("hearingAttendance", List.of(IN_PERSON)),
            Map.entry("hearingAttendanceDetails", "Attendance details"),
            Map.entry("preHearingAttendanceDetails", "Pre attendance details"),
            Map.entry("sendNoticeOfHearingTranslationRequirements", TRANSLATION_REQUIREMENTS),
            Map.entry("hearingDuration", HOURS_MINS.getType()),
            Map.entry("hearingHours", hours),
            Map.entry("hearingMinutes", minutes),
            Map.entry("hearingTypeReason", "Reason")
        ));
    }

    @Test
    void shouldBuildHearingBookingWhenNoPreviousVenueExists() {
        LocalDateTime startDate = time.now();
        LocalDateTime endDate = time.now().plusHours(1);

        CaseData caseData = CaseData.builder()
            .hearingType(CASE_MANAGEMENT)
            .hearingVenue(VENUE)
            .hearingAttendance(List.of(PHONE))
            .hearingStartDate(startDate)
            .hearingEndDate(endDate)
            .judgeAndLegalAdvisor(testJudgeAndLegalAdviser())
            .noticeOfHearingNotes("notes")
            .build();

        given(othersService.getSelectedOthers(caseData)).willReturn(SELECTED_OTHERS);

        HearingBooking hearingBooking = service.getCurrentHearingBooking(caseData);

        HearingBooking expectedHearingBooking = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .venue(VENUE)
            .attendance(List.of(PHONE))
            .startDate(startDate)
            .endDate(endDate)
            .hearingJudgeLabel("Her Honour Judge Judy")
            .legalAdvisorLabel(testJudgeAndLegalAdviser().getLegalAdvisorName())
            .judgeAndLegalAdvisor(testJudgeAndLegalAdviser())
            .others(SELECTED_OTHERS)
            .additionalNotes("notes")
            .endDateDerived("No")
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
            .hearingAttendance(List.of(VIDEO))
            .hearingStartDate(startDate)
            .hearingEndDate(endDate)
            .judgeAndLegalAdvisor(testJudgeAndLegalAdviser())
            .noticeOfHearingNotes("notes")
            .previousHearingVenue(previousHearingVenue)
            .build();

        given(othersService.getSelectedOthers(caseData)).willReturn(SELECTED_OTHERS);

        HearingBooking hearingBooking = service.getCurrentHearingBooking(caseData);

        HearingBooking expectedHearingBooking = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .venue(VENUE)
            .startDate(startDate)
            .endDate(endDate)
            .attendance(List.of(VIDEO))
            .hearingJudgeLabel("Her Honour Judge Judy")
            .legalAdvisorLabel(testJudgeAndLegalAdviser().getLegalAdvisorName())
            .judgeAndLegalAdvisor(testJudgeAndLegalAdviser())
            .others(SELECTED_OTHERS)
            .additionalNotes("notes")
            .previousHearingVenue(previousHearingVenue)
            .endDateDerived("No")
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

        given(othersService.getSelectedOthers(caseData)).willReturn(SELECTED_OTHERS);

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
            .others(SELECTED_OTHERS)
            .additionalNotes("notes")
            .previousHearingVenue(previousHearingVenue)
            .endDateDerived("No")
            .build();

        assertThat(hearingBooking).isEqualTo(expectedHearingBooking);
    }

    @Test
    void shouldSetHearingDurationInDaysWhenHearingInDaysIsSet() {
        LocalDateTime startDate = time.now();
        int hearingDays = 9;
        PreviousHearingVenue previousHearingVenue = PreviousHearingVenue.builder()
            .previousVenue("Custom House, Custom Street")
            .usePreviousVenue("Yes")
            .build();

        CaseData caseData = CaseData.builder()
            .previousVenueId("OTHER")
            .hearingType(CASE_MANAGEMENT)
            .hearingStartDate(startDate)
            .hearingDays(hearingDays)
            .judgeAndLegalAdvisor(testJudgeAndLegalAdviser())
            .noticeOfHearingNotes("notes")
            .previousHearingVenue(previousHearingVenue)
            .build();

        given(othersService.getSelectedOthers(caseData)).willReturn(SELECTED_OTHERS);

        HearingBooking hearingBooking = service.getCurrentHearingBooking(caseData);

        HearingBooking expectedHearingBooking = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .venue("OTHER")
            .customPreviousVenue("Custom House, Custom Street")
            .startDate(startDate)
            .hearingDuration("9 days")
            .endDateDerived(YES.getValue())
            .hearingJudgeLabel("Her Honour Judge Judy")
            .legalAdvisorLabel(testJudgeAndLegalAdviser().getLegalAdvisorName())
            .judgeAndLegalAdvisor(testJudgeAndLegalAdviser())
            .others(SELECTED_OTHERS)
            .additionalNotes("notes")
            .previousHearingVenue(previousHearingVenue)
            .hearingDays(9)
            .build();

        assertThat(hearingBooking).isEqualTo(expectedHearingBooking);
    }

    @Test
    void shouldSetHearingDurationInHoursAndMinutesWhenHearingInHoursAndMinutesIsSet() {
        LocalDateTime startDate = time.now();
        PreviousHearingVenue previousHearingVenue = PreviousHearingVenue.builder()
            .previousVenue("Custom House, Custom Street")
            .usePreviousVenue("Yes")
            .build();

        CaseData caseData = CaseData.builder()
            .previousVenueId("OTHER")
            .hearingType(CASE_MANAGEMENT)
            .hearingStartDate(startDate)
            .hearingHours(9)
            .hearingMinutes(45)
            .judgeAndLegalAdvisor(testJudgeAndLegalAdviser())
            .noticeOfHearingNotes("notes")
            .previousHearingVenue(previousHearingVenue)
            .build();

        given(othersService.getSelectedOthers(caseData)).willReturn(SELECTED_OTHERS);

        HearingBooking hearingBooking = service.getCurrentHearingBooking(caseData);

        HearingBooking expectedHearingBooking = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .venue("OTHER")
            .customPreviousVenue("Custom House, Custom Street")
            .startDate(startDate)
            .hearingDuration("9 hours 45 minutes")
            .endDateDerived(YES.getValue())
            .hearingJudgeLabel("Her Honour Judge Judy")
            .legalAdvisorLabel(testJudgeAndLegalAdviser().getLegalAdvisorName())
            .judgeAndLegalAdvisor(testJudgeAndLegalAdviser())
            .others(SELECTED_OTHERS)
            .additionalNotes("notes")
            .previousHearingVenue(previousHearingVenue)
            .hearingHours(9)
            .hearingMinutes(45)
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
    void shouldFindAndSetPreviousVenueIdWhenFlagNotPresent() {
        PreviousHearingVenue previousHearingVenue = PreviousHearingVenue.builder()
            .previousVenue("Custom House, Custom Street")
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
            .sendNoticeOfHearing(YES.getValue())
            .build();

        given(noticeOfHearingGenerationService.getTemplateData(caseData, hearingToUpdate))
            .willReturn(docmosisData);
        given(docmosisDocumentGeneratorService.generateDocmosisDocument(docmosisData, NOTICE_OF_HEARING))
            .willReturn(docmosisDocument);
        given(uploadDocumentService.uploadPDF(eq(docmosisDocument.getBytes()), anyString())).willReturn(DOCUMENT);

        service.buildNoticeOfHearingIfYes(caseData, hearingToUpdate);

        assertThat(hearingToUpdate.getNoticeOfHearing()).isEqualTo(DocumentReference.buildFromDocument(DOCUMENT));

        verify(noticeOfHearingGenerationService).getTemplateData(caseData, hearingToUpdate);
        verify(docmosisDocumentGeneratorService).generateDocmosisDocument(docmosisData, NOTICE_OF_HEARING);
        verify(uploadDocumentService).uploadPDF(
            TestDataHelper.DOCUMENT_CONTENT,
            NOTICE_OF_HEARING.getDocumentTitle(time.now().toLocalDate()));
    }

    @Test
    void shouldSendNoticeOfHearingIfRequestedWithTranslation() {
        final DocmosisNoticeOfHearing docmosisData = DocmosisNoticeOfHearing.builder().build();
        final DocmosisDocument docmosisDocument = testDocmosisDocument(TestDataHelper.DOCUMENT_CONTENT);

        final HearingBooking hearingToUpdate = randomHearing();
        final CaseData caseData = CaseData.builder()
            .sendNoticeOfHearing(YES.getValue())
            .sendNoticeOfHearingTranslationRequirements(TRANSLATION_REQUIREMENTS)
            .build();

        given(noticeOfHearingGenerationService.getTemplateData(caseData, hearingToUpdate))
            .willReturn(docmosisData);
        given(docmosisDocumentGeneratorService.generateDocmosisDocument(docmosisData, NOTICE_OF_HEARING))
            .willReturn(docmosisDocument);
        given(uploadDocumentService.uploadPDF(eq(docmosisDocument.getBytes()), anyString())).willReturn(DOCUMENT);

        service.buildNoticeOfHearingIfYes(caseData, hearingToUpdate);

        assertThat(hearingToUpdate.getNoticeOfHearing()).isEqualTo(DocumentReference.buildFromDocument(DOCUMENT));
        assertThat(hearingToUpdate.getTranslationRequirements()).isEqualTo(TRANSLATION_REQUIREMENTS);

        verify(noticeOfHearingGenerationService).getTemplateData(caseData, hearingToUpdate);
        verify(docmosisDocumentGeneratorService).generateDocmosisDocument(docmosisData, NOTICE_OF_HEARING);
        verify(uploadDocumentService).uploadPDF(
            TestDataHelper.DOCUMENT_CONTENT,
            NOTICE_OF_HEARING.getDocumentTitle(time.now().toLocalDate()));
    }


    @Nested
    class PastHearings {

        @Test
        void shouldSetStartDateHearingFieldsWhenHearingStartDateIsInThePast() {
            LocalDateTime hearingStartDate = LocalDateTime.of(2010, 3, 15, 20, 20);
            LocalDateTime hearingEndDate = LocalDateTime.now().plusDays(3);

            CaseData caseData = CaseData.builder()
                .hearingStartDate(hearingStartDate)
                .hearingEndDateTime(hearingEndDate)
                .hearingDuration(DATE_TIME.getType())
                .build();

            Map<String, Object> startDateFields = service.populateFieldsWhenPastHearingDateAdded(caseData);

            Map<String, Object> extractedFields = Map.of(
                "showConfirmPastHearingDatesPage", "Yes",
                "startDateFlag", "Yes",
                "hearingStartDateLabel", "15 March 2010, 8:20pm",
                "hearingEndDate", hearingEndDate);

            assertThat(startDateFields).isEqualTo(extractedFields);
        }

        @Test
        void shouldSetEndDateHearingFieldsWhenHearingEndDateIsInThePast() {
            LocalDateTime hearingStartDate = LocalDateTime.now().plusDays(3);
            LocalDateTime hearingEndDate = LocalDateTime.of(2010, 3, 15, 20, 20);

            CaseData caseData = CaseData.builder()
                .hearingStartDate(hearingStartDate)
                .hearingEndDateTime(hearingEndDate)
                .hearingDuration(DATE_TIME.getType())
                .build();

            Map<String, Object> startDateFields = service.populateFieldsWhenPastHearingDateAdded(caseData);

            Map<String, Object> extractedFields = Map.of(
                "showConfirmPastHearingDatesPage", "Yes",
                "endDateFlag", "Yes",
                "hearingDurationLabel", "15 March 2010, 8:20pm",
                "hearingEndDate", hearingEndDate);
            assertThat(startDateFields).isEqualTo(extractedFields);
        }

        @Test
        void shouldSetBothStartAndEndHearingFieldsWhenBothHearingDatesAreInThePast() {
            LocalDateTime hearingStartDate = LocalDateTime.of(2011, 4, 16, 20, 20);
            LocalDateTime hearingEndDate = LocalDateTime.of(2010, 3, 15, 20, 20);

            CaseData caseData = CaseData.builder()
                .hearingStartDate(hearingStartDate)
                .hearingEndDateTime(hearingEndDate)
                .hearingDuration(DATE_TIME.getType())
                .build();

            Map<String, Object> hearingDateFields = service.populateFieldsWhenPastHearingDateAdded(caseData);

            Map<String, Object> extractedFields = Map.of(
                "showConfirmPastHearingDatesPage", "Yes",
                "startDateFlag", "Yes",
                "hearingStartDateLabel", "16 April 2011, 8:20pm",
                "endDateFlag", "Yes",
                "hearingDurationLabel", "15 March 2010, 8:20pm",
                "hearingEndDate", hearingEndDate);

            assertThat(hearingDateFields).isEqualTo(extractedFields);
        }

        @Test
        void shouldSetBothStartAndEndHearingDaysWhenBothHearingDatesAreInThePast() {
            LocalDateTime hearingStartDate = LocalDateTime.of(2011, 4, 16, 20, 20);

            CaseData caseData = CaseData.builder()
                .hearingStartDate(hearingStartDate)
                .hearingDays(3)
                .hearingDuration(DAYS.getType())
                .build();

            Map<String, Object> hearingDateFields = service.populateFieldsWhenPastHearingDateAdded(caseData);

            Map<String, Object> extractedFields = Map.of(
                "showConfirmPastHearingDatesPage", "Yes",
                "startDateFlag", "Yes",
                "hearingStartDateLabel", "16 April 2011, 8:20pm",
                "endDateFlag", "Yes",
                "hearingDurationLabel", "3 days",
                "hearingEndDate", LocalDateTime.parse("2011-04-19T20:20:00"));


            assertThat(hearingDateFields).isEqualTo(extractedFields);
        }

        @Test
        void shouldSetBothStartAndEndHearingHoursAndMinutesWhenBothHearingDatesAreInThePast() {
            LocalDateTime hearingStartDate = LocalDateTime.of(2011, 4, 16, 20, 20);

            CaseData caseData = CaseData.builder()
                .hearingStartDate(hearingStartDate)
                .hearingHours(3)
                .hearingMinutes(20)
                .hearingDuration(HOURS_MINS.getType())
                .build();

            Map<String, Object> hearingDateFields = service.populateFieldsWhenPastHearingDateAdded(caseData);

            Map<String, Object> extractedFields = Map.of(
                "showConfirmPastHearingDatesPage", "Yes",
                "startDateFlag", "Yes",
                "hearingStartDateLabel", "16 April 2011, 8:20pm",
                "endDateFlag", "Yes",
                "hearingDurationLabel", "3 hours 20 minutes",
                "hearingEndDate", LocalDateTime.parse("2011-04-16T23:40:00"));

            assertThat(hearingDateFields).isEqualTo(extractedFields);
        }

        @Test
        void shouldThrowExceptionWhenHearingDurationNotSet() {
            LocalDateTime hearingStartDate = LocalDateTime.of(2011, 4, 16, 20, 20);

            CaseData caseData = CaseData.builder()
                .hearingStartDate(hearingStartDate)
                .hearingDuration("INVALID")
                .build();

            assertThatThrownBy(() -> service.populateFieldsWhenPastHearingDateAdded(caseData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid hearing duration INVALID");
        }

        @Test
        void shouldHidePastHearingsDatesPageWhenDatesAreInFuture() {
            LocalDateTime hearingStartDate = LocalDateTime.now().plusDays(1);
            LocalDateTime hearingEndDate = hearingStartDate.plusHours(1);

            CaseData caseData = CaseData.builder()
                .hearingStartDate(hearingStartDate)
                .hearingEndDateTime(hearingEndDate)
                .hearingDuration(DATE_TIME.getType())
                .build();

            Map<String, Object> hearingDateFields = service.populateFieldsWhenPastHearingDateAdded(caseData);

            Map<String, Object> extractedFields = Map.of(
                "showConfirmPastHearingDatesPage", "No",
                "hearingEndDate", hearingEndDate);

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
            Element<HearingBooking> hearing2 = element(hearing(time.now().plusDays(5), time.now().plusDays(6)));
            Element<HearingBooking> updatedHearing = element(hearing1.getId(),
                hearing(time.now().plusDays(4), time.now().plusDays(5)));

            CaseData caseData = CaseData.builder()
                .hearingDetails(List.of(hearing1, hearing2))
                .build();

            service.addOrUpdate(updatedHearing, caseData);

            assertThat(caseData.getHearingDetails()).containsExactly(hearing2, updatedHearing);
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
                .containsExactly(newHearing, hearing2, hearing1);
        }

        @Test
        void shouldSortByHearingStartDateWhenSameHearingDayExist() {
            LocalDateTime timeNow = time.now();
            Element<HearingBooking> hearing1 = element(hearing(timeNow.plusDays(1), time.now().plusDays(2)));
            Element<HearingBooking> hearing2 = element(hearing(timeNow.plusDays(2), time.now().plusDays(3)));
            Element<HearingBooking> hearing3 = element(hearing(timeNow.plusDays(3), time.now().plusDays(4)));
            Element<HearingBooking> newHearing = element(hearing(timeNow.plusDays(3), time.now().plusDays(4)));

            CaseData caseData = CaseData.builder()
                .hearingDetails(newArrayList(hearing2, hearing3, hearing1))
                .build();

            service.addOrUpdate(newHearing, caseData);

            assertThat(caseData.getHearingDetails())
                .containsExactly(hearing3, newHearing, hearing2, hearing1);
        }

        @Test
        void shouldSortByHearingStartDateWhenExistingHearingAreAlreadySorted() {
            LocalDateTime timeNow = time.now();
            Element<HearingBooking> hearing1 = element(hearing(timeNow.plusDays(1), time.now().plusDays(2)));
            Element<HearingBooking> hearing2 = element(hearing(timeNow.plusHours(1), time.now().plusDays(4)));
            Element<HearingBooking> hearing3 = element(hearing(timeNow.plusDays(2), time.now().plusDays(3)));
            Element<HearingBooking> hearing4 = element(hearing(timeNow.plusDays(4), time.now().plusDays(5)));
            Element<HearingBooking> newHearing = element(hearing(timeNow.plusDays(4), time.now().plusDays(5)));

            CaseData caseData = CaseData.builder()
                .hearingDetails(newArrayList(hearing4, newHearing, hearing3, hearing1, hearing2))
                .build();

            service.addOrUpdate(newHearing, caseData);

            assertThat(caseData.getHearingDetails())
                .containsExactly(hearing4, newHearing, hearing3, hearing1, hearing2);
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

            final CaseData caseData = CaseData.builder()
                .hearingDetails(newArrayList(hearingToBeAdjourned, otherHearing))
                .adjournmentReason(adjournmentReason)
                .build();

            service.adjournAndReListHearing(caseData, hearingToBeAdjourned.getId(), reListedHearing.getValue());

            assertThat(caseData.getHearingDetails()).containsExactly(otherHearing, reListedHearing);
            assertThat(caseData.getCancelledHearingDetails()).containsExactly(adjournedHearing);
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

            CaseData caseData = CaseData.builder()
                .hearingDetails(newArrayList(hearingElement1, hearingElement2))
                .adjournmentReason(adjournmentReason)
                .hearingReListOption(hearingReListOption)
                .build();

            service.adjournHearing(caseData, hearingElement1.getId());

            assertThat(caseData.getHearingDetails()).containsExactly(hearingElement2);
            assertThat(caseData.getCancelledHearingDetails()).containsExactly(adjournedHearing);
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
        private final LocalDate vacatedDate = time.now().minusDays(1).toLocalDate();

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
                    .vacatedDate(vacatedDate)
                    .build());

            CaseData caseData = CaseData.builder()
                .hearingDetails(newArrayList(hearingElement1, hearingElement2))
                .vacatedReason(vacatedReason)
                .vacatedHearingDate(vacatedDate)
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
                    .vacatedDate(vacatedDate)
                    .build());

            CaseData caseData = CaseData.builder()
                .hearingDetails(newArrayList(hearingElement1, hearingElement2))
                .vacatedHearingDate(vacatedDate)
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
                    .vacatedDate(vacatedDate)
                    .build());

            CaseData caseData = CaseData.builder()
                .hearingDetails(newArrayList(hearingToBeVacated, otherHearing))
                .vacatedReason(vacatedReason)
                .vacatedHearingDate(vacatedDate)
                .build();

            service.vacateAndReListHearing(caseData, hearingToBeVacated.getId(), reListedHearing.getValue());

            assertThat(caseData.getHearingDetails()).containsExactly(otherHearing, reListedHearing);
            assertThat(caseData.getCancelledHearingDetails()).containsExactly(expectedVacatedHearing);
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
                    .vacatedDate(vacatedDate)
                    .build());

            final CaseData caseData = CaseData.builder()
                .hearingDetails(newArrayList(hearingToBeVacated, otherHearing))
                .vacatedReason(vacatedReason)
                .vacatedHearingDate(vacatedDate)
                .build();

            service.vacateAndReListHearing(caseData, hearingToBeVacated.getId(), reListedHearing.getValue());

            assertThat(caseData.getHearingDetails()).containsExactly(otherHearing, reListedHearing);
            assertThat(caseData.getCancelledHearingDetails()).containsExactly(vacatedHearing);
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
                    .vacatedDate(vacatedDate)
                    .build());

            final Element<HearingOrder> linkedDraftCMO = element(LINKED_CMO_ID, HearingOrder.builder().build());

            final CaseData caseData = CaseData.builder()
                .hearingDetails(newArrayList(hearingToBeVacated, otherHearing))
                .vacatedReason(vacatedReason)
                .vacatedHearingDate(vacatedDate)
                .draftUploadedCMOs(List.of(linkedDraftCMO))
                .build();

            service.vacateAndReListHearing(caseData, hearingToBeVacated.getId(), reListedHearing.getValue());

            assertThat(caseData.getHearingDetails()).containsExactly(otherHearing, reListedHearing);
            assertThat(caseData.getCancelledHearingDetails()).containsExactly(vacatedHearing);
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
                    .vacatedDate(vacatedDate)
                    .build());

            final Element<HearingOrder> linkedDraftCMO = element(LINKED_CMO_ID, HearingOrder.builder().build());

            final CaseData caseData = CaseData.builder()
                .hearingDetails(newArrayList(hearingToBeVacated))
                .vacatedReason(vacatedReason)
                .vacatedHearingDate(vacatedDate)
                .draftUploadedCMOs(List.of(linkedDraftCMO))
                .build();

            service.vacateAndReListHearing(caseData, hearingToBeVacated.getId(), reListedHearing.getValue());

            assertThat(caseData.getHearingDetails()).containsExactly(reListedHearing);
            assertThat(caseData.getCancelledHearingDetails()).containsExactly(vacatedHearing);
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
                    .vacatedDate(vacatedDate)
                    .build());

            final Element<HearingOrder> linkedDraftCMO = element(LINKED_CMO_ID, HearingOrder.builder()
                .type(DRAFT_CMO).status(DRAFT)
                .build());

            final CaseData caseData = CaseData.builder()
                .hearingDetails(newArrayList(hearingToBeVacated, otherHearing))
                .vacatedReason(vacatedReason)
                .vacatedHearingDate(vacatedDate)
                .hearingOrdersBundlesDrafts(newArrayList(
                    element(HEARING_BUNDLE_ID, HearingOrdersBundle.builder()
                        .orders(newArrayList(linkedDraftCMO))
                        .build())
                ))
                .build();

            service.vacateAndReListHearing(caseData, hearingToBeVacated.getId(), reListedHearing.getValue());

            assertThat(caseData.getHearingDetails()).containsExactly(otherHearing, reListedHearing);
            assertThat(caseData.getCancelledHearingDetails()).containsExactly(vacatedHearing);
            assertThat(caseData.getHearingOrdersBundlesDrafts()).contains(
                element(HEARING_BUNDLE_ID, HearingOrdersBundle.builder()
                    .hearingName(vacatedHearing.getValue().toLabel())
                    .orders(newArrayList(element(linkedDraftCMO.getId(),
                        linkedDraftCMO.getValue().toBuilder().hearing(vacatedHearing.getValue().toLabel()).build())))
                    .build()));
        }

        @Test
        void shouldHousekeepHearing() {
            HearingCancellationReason vacatedReason = HearingCancellationReason.builder()
                .reason("Reason 1")
                .build();

            Element<HearingBooking> hearingElement1 = element(hearing(time.now().plusDays(1), time.now().plusDays(2)));
            Element<HearingBooking> hearingElement2 = element(hearing(time.now().plusDays(2), time.now().plusDays(3)));

            Element<HearingBooking> vacatedHearing = element(hearingElement1.getId(),
                hearingElement1.getValue().toBuilder()
                    .status(VACATED)
                    .housekeepReason(HearingHousekeepReason.DUPLICATE.getLabel())
                    .vacatedDate(vacatedDate)
                    .build());

            final Element<HearingFurtherEvidenceBundle> vacatedHearingBundle = randomDocumentBundle(hearingElement1);

            CaseData caseData = CaseData.builder()
                .hearingDetails(newArrayList(hearingElement1, hearingElement2))
                .hearingReListOption(NONE)
                .manageHearingHousekeepEventData(ManageHearingHousekeepEventData.builder()
                    .hearingHousekeepOptions(YES)
                    .hearingHousekeepReason(HearingHousekeepReason.DUPLICATE)
                    .build())
                .vacatedHearingDate(vacatedDate)
                .hearingFurtherEvidenceDocuments(List.of(vacatedHearingBundle))
                .build();

            final String documentBundleName = vacatedHearingBundle.getValue().getHearingName();
            final String updatedDocumentBundleName = String.format("%s - %s", documentBundleName, "vacated");

            service.vacateHearing(caseData, hearingElement1.getId());

            assertThat(caseData.getHearingDetails()).containsExactly(hearingElement2);
            assertThat(caseData.getCancelledHearingDetails()).containsExactly(vacatedHearing);
            assertThat(vacatedHearingBundle.getValue().getHearingName()).isEqualTo(updatedDocumentBundleName);
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
                    .vacatedDate(vacatedDate)
                    .build());

            CaseData caseData = CaseData.builder()
                .hearingDetails(newArrayList(hearingElement1, hearingElement2))
                .hearingReListOption(reListOption)
                .vacatedReason(vacatedReason)
                .vacatedHearingDate(vacatedDate)
                .build();

            service.vacateHearing(caseData, hearingElement1.getId());

            assertThat(caseData.getHearingDetails()).containsExactly(hearingElement2);
            assertThat(caseData.getCancelledHearingDetails()).containsExactly(vacatedHearing);
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

    }

    @Nested
    class HearingsDynamicList {

        @Test
        void shouldReturnVacateDateListWhenHearingOptionIsVacateHearing() {
            CaseData caseData = caseData(VACATE_HEARING);

            Object dynamicList = service.getHearingsDynamicList(caseData);
            assertThat(dynamicList).isEqualTo(caseData.getVacateHearingDateList());
        }

        @Test
        void shouldReturnPastAndTodayHearingDateListWhenHearingOptionIsAdjournHearing() {
            CaseData caseData = caseData(ADJOURN_HEARING);

            Object dynamicList = service.getHearingsDynamicList(caseData);
            assertThat(dynamicList).isEqualTo(caseData.getPastAndTodayHearingDateList());
        }

        @Test
        void shouldReturnFutureHearingsWhenHearingOptionIsEdit() {
            CaseData caseData = caseData(EDIT_FUTURE_HEARING);

            Object dynamicList = service.getHearingsDynamicList(caseData);
            assertThat(dynamicList).isEqualTo(caseData.getFutureHearingDateList());
        }

        @Test
        void shouldReturnPastHearingsWhenHearingOptionIsEdit() {
            CaseData caseData = caseData(EDIT_PAST_HEARING);

            Object dynamicList = service.getHearingsDynamicList(caseData);
            assertThat(dynamicList).isEqualTo(caseData.getPastHearingDateList());
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
                .vacateHearingDateList(randomDynamicList())
                .toReListHearingDateList(randomDynamicList())
                .pastHearingDateList(randomDynamicList())
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
        final UUID selectedFutureHearing = randomUUID();
        final UUID selectedToReListHearing = randomUUID();
        final UUID selectedHearing = randomUUID();

        @Test
        void shouldReturnSelectedHearingIdWhenHearingOptionIsVacateHearing() {
            CaseData caseData = caseData(VACATE_HEARING);

            UUID selectedHearingId = service.getSelectedHearingId(caseData);
            assertThat(selectedHearingId).isEqualTo(selectedHearing);
        }

        @Test
        void shouldReturnSelectedHearingIdWhenHearingOptionIsAdjournHearing() {
            CaseData caseData = caseData(ADJOURN_HEARING);

            UUID selectedHearingId = service.getSelectedHearingId(caseData);
            assertThat(selectedHearingId).isEqualTo(selectedPastAndTodayHearing);
        }

        @Test
        void shouldReturnSelectedPastHearingIdWhenHearingOptionIsEdit() {
            CaseData caseData = caseData(EDIT_PAST_HEARING);

            UUID selectedHearingId = service.getSelectedHearingId(caseData);
            assertThat(selectedHearingId).isEqualTo(selectedHearing);
        }

        @Test
        void shouldReturnSelectedFutureHearingIdWhenHearingOptionIsEdit() {
            CaseData caseData = caseData(EDIT_FUTURE_HEARING);

            UUID selectedHearingId = service.getSelectedHearingId(caseData);
            assertThat(selectedHearingId).isEqualTo(selectedFutureHearing);
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
                .futureHearingDateList(randomDynamicList(selectedFutureHearing))
                .vacateHearingDateList(randomDynamicList(selectedHearing))
                .toReListHearingDateList(randomDynamicList(selectedToReListHearing))
                .pastHearingDateList(randomDynamicList(selectedHearing))
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

    @Test
    void shouldReturnFieldsToBeDeleted() {

        assertThat(service.caseFieldsToBeRemoved()).containsExactlyInAnyOrder(
            "hearingType",
            "hearingTypeDetails",
            "hearingTypeReason",
            "hearingVenue",
            "hearingVenueCustom",
            "hearingStartDate",
            "hearingEndDate",
            "sendNoticeOfHearing",
            "sendNoticeOfHearingTranslationRequirements",
            "judgeAndLegalAdvisor",
            "noticeOfHearingNotes",
            "previousHearingVenue",
            "firstHearingFlag",
            "hasPreviousHearingVenue",
            "adjournmentReason",
            "vacatedReason",
            "pastHearingDateList",
            "futureHearingDateList",
            "pastAndTodayHearingDateList",
            "vacateHearingDateList",
            "vacatedHearingDate",
            "hasHearingsToAdjourn",
            "hasPastHearings",
            "hasFutureHearings",
            "hasExistingHearings",
            "hearingReListOption",
            "hearingStartDateLabel",
            "showConfirmPastHearingDatesPage",
            "hearingDurationLabel",
            "confirmHearingDate",
            "hearingStartDateConfirmation",
            "hearingEndDateConfirmation",
            "startDateFlag",
            "endDateFlag",
            "hasSession",
            "hearingAttendance",
            "preHearingAttendanceDetails",
            "hearingAttendanceDetails",
            "hearingOption",
            "hasOthers",
            "sendOrderToAllOthers",
            "others_label",
            "othersSelector",
            "hearingMinutes",
            "hearingDuration",
            "hearingDays",
            "hearingHours",
            "hearingEndDateTime",
            "hearingJudge",
            "enterManuallyHearingJudge",
            "useAllocatedJudge",
            "allocatedJudgeLabel",
            "judicialUser",
            "enterManually",
            "judicialUserHearingJudge"
        );
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
