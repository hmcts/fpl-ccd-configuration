package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.gov.hmcts.reform.fpl.enums.HearingStatus;
import uk.gov.hmcts.reform.fpl.enums.hearing.HearingAttendance;
import uk.gov.hmcts.reform.fpl.enums.hearing.HearingPresence;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingNeedsBooked.INTERPRETER;
import static uk.gov.hmcts.reform.fpl.enums.HearingNeedsBooked.NONE;
import static uk.gov.hmcts.reform.fpl.enums.HearingNeedsBooked.SOMETHING_ELSE;
import static uk.gov.hmcts.reform.fpl.enums.HearingNeedsBooked.SPOKEN_OR_WRITTEN_WELSH;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.hearing.HearingAttendance.IN_PERSON;
import static uk.gov.hmcts.reform.fpl.enums.hearing.HearingAttendance.PHONE;
import static uk.gov.hmcts.reform.fpl.enums.hearing.HearingAttendance.VIDEO;

class HearingBookingTest {

    @Test
    void shouldReturnEmptyListWhenNoHearingNeedsBooked() {
        HearingBooking hearing = HearingBooking.builder().build();
        assertThat(hearing.buildHearingNeedsList()).isEmpty();
    }

    @Test
    void shouldReturnValidListWhenHearingNeedsBooked() {
        HearingBooking hearing = HearingBooking.builder()
            .hearingNeedsBooked(List.of(INTERPRETER, SPOKEN_OR_WRITTEN_WELSH, SOMETHING_ELSE))
            .build();

        assertThat(hearing.buildHearingNeedsList()).containsExactly(INTERPRETER.getLabel(),
            SPOKEN_OR_WRITTEN_WELSH.getLabel());
    }

    @Test
    void shouldReturnEmptyListWhenNoneSelected() {
        HearingBooking hearing = HearingBooking.builder()
            .hearingNeedsBooked(List.of(NONE))
            .build();
        assertThat(hearing.buildHearingNeedsList()).isEmpty();
    }

    @Test
    void shouldReturnTrueIfHearingTypeIsOfTypeFinal() {
        HearingBooking hearingBooking = HearingBooking.builder().type(FINAL).build();
        assertThat(hearingBooking.isOfType(FINAL)).isTrue();
    }

    @Test
    void shouldReturnFalseIfHearingTypeIsNotOfTypeFinal() {
        HearingBooking hearingBooking = HearingBooking.builder().type(CASE_MANAGEMENT).build();
        assertThat(hearingBooking.isOfType(FINAL)).isFalse();
    }

    @Nested
    class StartsAfterToday {

        @Test
        void shouldReturnFalseForHearingStartedToday() {
            HearingBooking hearingBooking = HearingBooking.builder()
                .startDate(LocalDateTime.now())
                .build();

            assertThat(hearingBooking.startsAfterToday()).isFalse();
        }

        @Test
        void shouldReturnFalseForHearingStartedInPast() {
            HearingBooking hearingBooking = HearingBooking.builder()
                .startDate(LocalDateTime.now().minusDays(1))
                .build();

            assertThat(hearingBooking.startsAfterToday()).isFalse();
        }

        // @Test todo - fix this timezone math
        void shouldReturnTrueForHearingStartingLaterToday() {
            HearingBooking hearingBooking = HearingBooking.builder()
                .startDate(LocalDate.now().plusDays(1).atStartOfDay().minusSeconds(1))
                .build();

            assertThat(hearingBooking.startsAfterToday()).isTrue();
        }

        @Test
        void shouldReturnTrueForHearingStartingAfterToday() {
            HearingBooking hearingBooking = HearingBooking.builder()
                .startDate(LocalDateTime.now().plusDays(1))
                .build();

            assertThat(hearingBooking.startsAfterToday()).isTrue();
        }

        @Test
        void shouldReturnFalseForHearingWithoutStartDate() {
            HearingBooking hearingBooking = HearingBooking.builder().build();

            assertThat(hearingBooking.startsAfterToday()).isFalse();
        }
    }

    @Nested
    class StartsTodayOrBefore {

        @Test
        void shouldReturnTrueForHearingStartedToday() {
            HearingBooking hearingBooking = HearingBooking.builder()
                .startDate(LocalDateTime.now())
                .build();

            assertThat(hearingBooking.startsTodayOrBefore()).isTrue();
        }

        @Test
        void shouldReturnTrueForHearingStartedInPast() {
            HearingBooking hearingBooking = HearingBooking.builder()
                .startDate(LocalDateTime.now().minusDays(1))
                .build();

            assertThat(hearingBooking.startsTodayOrBefore()).isTrue();
        }

        @Test
        void shouldReturnTrueForHearingStaringLaterToday() {
            HearingBooking hearingBooking = HearingBooking.builder()
                .startDate(LocalDate.now().plusDays(1).atStartOfDay().minusSeconds(1))
                .build();

            assertThat(hearingBooking.startsTodayOrBefore()).isTrue();
        }

        @Test
        void shouldReturnFalseForHearingStartingAfterToday() {
            HearingBooking hearingBooking = HearingBooking.builder()
                .startDate(LocalDate.now().plusDays(1).atStartOfDay().minusSeconds(1))
                .build();

            assertThat(hearingBooking.startsTodayOrBefore()).isTrue();
        }

        @Test
        void shouldReturnFalseForHearingWithoutStartDate() {
            HearingBooking hearingBooking = HearingBooking.builder().build();

            assertThat(hearingBooking.startsTodayOrBefore()).isFalse();
        }
    }

    @Nested
    class StartsTodayOrAfter {

        @Test
        void shouldReturnTrueForHearingStartedToday() {
            HearingBooking hearingBooking = HearingBooking.builder()
                .startDate(LocalDateTime.now())
                .build();

            assertThat(hearingBooking.startsTodayOrAfter()).isTrue();
        }

        @Test
        void shouldReturnFalseForHearingStartedInPast() {
            HearingBooking hearingBooking = HearingBooking.builder()
                .startDate(LocalDateTime.now().minusDays(1))
                .build();

            assertThat(hearingBooking.startsTodayOrAfter()).isFalse();
        }

        @Test
        void shouldReturnTrueForHearingStaringLaterToday() {
            HearingBooking hearingBooking = HearingBooking.builder()
                .startDate(LocalDate.now().plusDays(1).atStartOfDay().minusSeconds(1))
                .build();

            assertThat(hearingBooking.startsTodayOrAfter()).isTrue();
        }

        @Test
        void shouldReturnTrueForHearingStartingAfterToday() {
            HearingBooking hearingBooking = HearingBooking.builder()
                .startDate(LocalDate.now().plusDays(1).atStartOfDay().minusSeconds(1))
                .build();

            assertThat(hearingBooking.startsTodayOrAfter()).isTrue();
        }

        @Test
        void shouldReturnFalseForHearingWithoutStartDate() {
            HearingBooking hearingBooking = HearingBooking.builder().build();

            assertThat(hearingBooking.startsTodayOrAfter()).isFalse();
        }
    }

    @Nested
    class Status {

        @ParameterizedTest
        @EnumSource(
            value = HearingStatus.class,
            names = {"ADJOURNED", "ADJOURNED_TO_BE_RE_LISTED", "ADJOURNED_AND_RE_LISTED"})
        void shouldReturnTrueForAdjournedHearings(HearingStatus hearingStatus) {
            HearingBooking hearingBooking = HearingBooking.builder().status(hearingStatus).build();

            assertThat(hearingBooking.isAdjourned()).isTrue();
            assertThat(hearingBooking.isVacated()).isFalse();
        }

        @ParameterizedTest
        @EnumSource(
            value = uk.gov.hmcts.reform.fpl.enums.HearingStatus.class,
            names = {"VACATED", "VACATED_TO_BE_RE_LISTED", "VACATED_AND_RE_LISTED"})
        void shouldReturnTrueForVacatedHearings(HearingStatus hearingStatus) {
            HearingBooking hearingBooking = HearingBooking.builder().status(hearingStatus).build();

            assertThat(hearingBooking.isVacated()).isTrue();
            assertThat(hearingBooking.isAdjourned()).isFalse();
        }

        @ParameterizedTest
        @EnumSource(
            value = HearingStatus.class,
            names = {"ADJOURNED_TO_BE_RE_LISTED", "VACATED_TO_BE_RE_LISTED"})
        void shouldReturnTrueForToBeReListedHearings(HearingStatus hearingStatus) {
            HearingBooking hearingBooking = HearingBooking.builder().status(hearingStatus).build();

            assertThat(hearingBooking.isToBeReListed()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(
            value = HearingStatus.class,
            names = {"ADJOURNED_TO_BE_RE_LISTED", "VACATED_TO_BE_RE_LISTED"},
            mode = EnumSource.Mode.EXCLUDE)
        void shouldReturnFalseForNotToBeReListedHearings(HearingStatus hearingStatus) {
            HearingBooking hearingBooking = HearingBooking.builder().status(hearingStatus).build();

            assertThat(hearingBooking.isToBeReListed()).isFalse();
        }

        @Test
        void shouldHandleNullStatus() {
            HearingBooking hearingBooking = HearingBooking.builder().status(null).build();

            assertThat(hearingBooking.isToBeReListed()).isFalse();
            assertThat(hearingBooking.isAdjourned()).isFalse();
            assertThat(hearingBooking.isVacated()).isFalse();
        }

    }

    @Nested
    class Label {

        @Test
        void shouldBuildHearingLabelWithoutStatus() {
            HearingBooking hearingBooking = HearingBooking.builder()
                .type(CASE_MANAGEMENT)
                .startDate(LocalDate.of(2020, 10, 10).atStartOfDay())
                .build();

            assertThat(hearingBooking.toLabel()).isEqualTo("Case management hearing, 10 October 2020");
        }

        @ParameterizedTest
        @EnumSource(value = HearingStatus.class,
            names = {"ADJOURNED", "ADJOURNED_TO_BE_RE_LISTED", "ADJOURNED_AND_RE_LISTED"})
        void shouldBuildLabelWithStatusForAdjournedHearings(HearingStatus hearingStatus) {
            HearingBooking hearingBooking = HearingBooking.builder()
                .type(CASE_MANAGEMENT)
                .status(hearingStatus)
                .startDate(LocalDate.of(2020, 10, 1).atStartOfDay())
                .build();

            assertThat(hearingBooking.toLabel()).isEqualTo("Case management hearing, 1 October 2020 - adjourned");
        }

        @ParameterizedTest
        @EnumSource(value = HearingStatus.class,
            names = {"VACATED", "VACATED_TO_BE_RE_LISTED", "VACATED_AND_RE_LISTED"})
        void shouldBuildLabelWithStatusForVacatedHearings(HearingStatus hearingStatus) {
            HearingBooking hearingBooking = HearingBooking.builder()
                .type(CASE_MANAGEMENT)
                .status(hearingStatus)
                .startDate(LocalDate.of(2020, 10, 30).atStartOfDay())
                .build();

            assertThat(hearingBooking.toLabel()).isEqualTo("Case management hearing, 30 October 2020 - vacated");
        }

        @Test
        void shouldReturnOtherLabelWhenTypeIsSetNull() {
            HearingBooking hearingBooking = HearingBooking.builder()
                .type(null)
                .startDate(LocalDate.of(2020, 10, 10).atStartOfDay())
                .build();

            assertThat(hearingBooking.toLabel()).isEqualTo("Other hearing, 10 October 2020");
        }

    }

    @Nested
    class PreAttendance {

        @Test
        void shouldReturnExistingPreHearingAttendanceDetails() {
            HearingBooking hearingBooking = HearingBooking.builder()
                .preAttendanceDetails("Test")
                .build();

            assertThat(hearingBooking.getPreAttendanceDetails()).isEqualTo("Test");
        }

        @Test
        void shouldReturnDefaultPreHearingAttendanceDetails() {
            HearingBooking hearingBooking = HearingBooking.builder()
                .build();

            assertThat(hearingBooking.getPreAttendanceDetails()).isEqualTo("1 hour before the hearing");
        }
    }

    @Nested
    class Attendance {

        @Test
        void shouldReturnAttendance() {
            HearingBooking hearingBooking = HearingBooking.builder()
                .attendance(List.of(IN_PERSON, PHONE))
                .build();

            assertThat(hearingBooking.getAttendance()).containsExactlyInAnyOrder(IN_PERSON, PHONE);
        }

        @Test
        void shouldConvertLegacyInPersonPresenceIntoAttendanceWhenAttendanceNotPresent() {
            HearingBooking hearingBooking = HearingBooking.builder()
                .presence(HearingPresence.IN_PERSON)
                .build();

            assertThat(hearingBooking.getAttendance()).containsExactly(IN_PERSON);
        }

        @Test
        void shouldConvertLegacyRemotePresenceIntoVideoAttendanceWhenAttendanceNotPresent() {
            HearingBooking hearingBooking = HearingBooking.builder()
                .presence(HearingPresence.REMOTE)
                .build();

            assertThat(hearingBooking.getAttendance()).containsExactly(VIDEO);
        }

        @ParameterizedTest
        @EnumSource(HearingPresence.class)
        void shouldNotConvertLegacyPresenceIntoAttendanceWhenAttendancePresent(HearingPresence presence) {
            HearingBooking hearingBooking = HearingBooking.builder()
                .presence(presence)
                .attendance(List.of(PHONE))
                .build();

            assertThat(hearingBooking.getAttendance()).containsExactly(PHONE);
        }
    }

    @Nested
    class IsRemote {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnFalseWhenAttendanceIsNotSpecified(List<HearingAttendance> attendance) {
            HearingBooking hearingBooking = HearingBooking.builder()
                .attendance(attendance)
                .build();

            assertThat(hearingBooking.isRemote()).isFalse();
        }

        @Test
        void shouldReturnFalseWhenNoRemoteHearingAttendanceAllowed() {
            HearingBooking hearingBooking = HearingBooking.builder()
                .attendance(List.of(IN_PERSON))
                .build();

            assertThat(hearingBooking.isRemote()).isFalse();
        }

        @ParameterizedTest
        @EnumSource(value = HearingAttendance.class, names = {"VIDEO", "PHONE",})
        void shouldReturnTrueWhenOnlyRemoteHearingAttendanceAllowed(HearingAttendance hearingAttendance) {
            HearingBooking hearingBooking = HearingBooking.builder()
                .attendance(List.of(hearingAttendance))
                .build();

            assertThat(hearingBooking.isRemote()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = HearingAttendance.class, names = {"VIDEO", "PHONE",})
        void shouldReturnTrueWhenRemoteHearingAttendanceAllowed(HearingAttendance hearingAttendance) {
            HearingBooking hearingBooking = HearingBooking.builder()
                .attendance(List.of(hearingAttendance, IN_PERSON))
                .build();

            assertThat(hearingBooking.isRemote()).isTrue();
        }
    }

    @Nested
    class EndDate {

        @Test
        void shouldReturnCorrectEndDateForSingleDayHearing() {
            HearingBooking hearingBooking = HearingBooking.builder()
                .startDate(LocalDateTime.of(2022, 11, 28, 0, 0, 0))
                .hearingDays(1)
                .build();

            assertThat(hearingBooking.getEndDate()).isEqualTo(LocalDateTime.of(2022, 11, 28, 0, 0, 0));
        }

        @Test
        void shouldReturnCorrectEndDateWithNoWeekend() {
            HearingBooking hearingBooking = HearingBooking.builder()
                .startDate(LocalDateTime.of(2022, 11, 28, 0, 0, 0))
                .hearingDays(4)
                .build();

            assertThat(hearingBooking.getEndDate()).isEqualTo(LocalDateTime.of(2022, 12, 1, 0, 0, 0));
        }

        @Test
        void shouldReturnCorrectEndDateWithOneWeekend() {
            HearingBooking hearingBooking = HearingBooking.builder()
                .startDate(LocalDateTime.of(2022, 12, 7, 0, 0, 0))
                .hearingDays(5)
                .build();

            assertThat(hearingBooking.getEndDate()).isEqualTo(LocalDateTime.of(2022, 12, 13, 0, 0, 0));
        }

        @Test
        void shouldReturnCorrectEndDateWithThreeWeekends() {
            HearingBooking hearingBooking = HearingBooking.builder()
                .startDate(LocalDateTime.of(2022, 11, 28, 0, 0, 0))
                .hearingDays(16)
                .build();

            assertThat(hearingBooking.getEndDate()).isEqualTo(LocalDateTime.of(2022, 12, 19, 0, 0, 0));
        }
    }
}
