package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.AgeFormatHelper.formatAge;

class AgeFormatHelperTest {
    @Test
    void shouldReturnAgeAs12YearsOld() {
        String returnedAge = formatAge(now().minusYears(12));
        assertThat(returnedAge).isEqualTo("12 years old");
    }

    @Test
    void shouldReturnAgeAs1YearOld() {
        String returnedAge = formatAge(now().minusYears(1));
        assertThat(returnedAge).isEqualTo("1 year old");
    }

    @Test
    void shouldReturnAgeAs1MonthOld() {
        String returnedAge = formatAge(now().minusMonths(1));
        assertThat(returnedAge).isEqualTo("1 month old");
    }

    @Test
    void shouldReturnAgeAs1Year1MonthOld() {
        String returnedAge = formatAge(now().minusYears(1).minusMonths(1));
        assertThat(returnedAge).isEqualTo("1 year 1 month old");
    }

    @Test
    void shouldReturnAgeAs10Years2MonthsOld() {
        String returnedAge = formatAge(now().minusYears(10).minusMonths(2));
        assertThat(returnedAge).isEqualTo("10 years 2 months old");
    }

    @Test
    void shouldReturnAgeAs2Years1DayOld() {
        String returnedAge = formatAge(now().minusYears(2).minusDays(1));
        assertThat(returnedAge).isEqualTo("2 years 1 day old");
    }

    @Test
    void shouldReturnAgeAs1DayOld() {
        String returnedAge = formatAge(now().minusDays(1));
        assertThat(returnedAge).isEqualTo("1 day old");
    }

    @Test
    void shouldReturnAgeAs2Years2DaysOld() {
        String returnedAge = formatAge(now().minusYears(2).minusDays(2));
        assertThat(returnedAge).isEqualTo("2 years 2 days old");
    }

    @Test
    void shouldReturnAgeAs2Years1Month2DaysOld() {
        String returnedAge = formatAge(now().minusYears(2).minusMonths(1).minusDays(2));
        assertThat(returnedAge).isEqualTo("2 years 1 month 2 days old");
    }
}
