package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
}
