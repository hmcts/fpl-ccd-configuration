package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.utils.AgeDisplayFormatHelper.formatAgeDisplay;

/**
 * AgeDisplayFormatHelper calculates age based on provided date and returns it in string format.
 */
class AgeDisplayFormatHelperTest {
    private static final LocalDate NOW = LocalDate.now();

    @Test
    void shouldThrowExceptionWhenInputIsNotProvided() {
        assertThatThrownBy(() -> formatAgeDisplay(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Date of birth value is required");
    }

    @Test
    void shouldThrowExceptionWhenInputIsInFuture() {
        String age = formatAgeDisplay(NOW.plusDays(1));
        assertThat(age).isEqualTo("0 years old");
    }

    @Test
    void shouldReturn0DaysWhenBornToday() {
        String age = formatAgeDisplay(NOW);
        assertThat(age).isEqualTo("0 days old");
    }

    @Test
    void shouldReturn1DayWhenBornYesterday() {
        String age = formatAgeDisplay(NOW.minusDays(1));
        assertThat(age).isEqualTo("1 day old");
    }

    @Test
    void shouldReturn2DaysWhenBornDateBeforeYesterday() {
        String age = formatAgeDisplay(NOW.minusDays(2));
        assertThat(age).isEqualTo("2 days old");
    }

    @Test
    void shouldReturn1MonthWhenBorn1MonthAgo() {
        String age = formatAgeDisplay(NOW.minusMonths(1));
        assertThat(age).isEqualTo("1 month old");
    }

    @Test
    void shouldReturn1MonthWhenBorn1MonthAnd1DayAgo() {
        String age = formatAgeDisplay(NOW.minusMonths(1).minusDays(1));
        assertThat(age).isEqualTo("1 month old");
    }

    @Test
    void shouldReturn2MonthsWhenBorn2MonthsAgo() {
        String age = formatAgeDisplay(NOW.minusMonths(2));
        assertThat(age).isEqualTo("2 months old");
    }

    @Test
    void shouldReturn1YearWhenBorn1YearAgo() {
        String age = formatAgeDisplay(NOW.minusYears(1));
        assertThat(age).isEqualTo("1 year old");
    }

    @Test
    void shouldReturn1YearWhenBorn1YearAnd1MonthAgo() {
        String age = formatAgeDisplay(NOW.minusYears(1).minusMonths(1));
        assertThat(age).isEqualTo("1 year old");
    }

    @Test
    void shouldReturn2YearsWhenBorn2YearsAgo() {
        String age = formatAgeDisplay(NOW.minusYears(2));
        assertThat(age).isEqualTo("2 years old");
    }
}
