package uk.gov.hmcts.reform.pebble;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static org.assertj.core.api.Assertions.assertThat;

class AgeFilterTest {

    private static final ImmutableMap<String, Object> NO_ARGS = ImmutableMap.<String, Object>builder().build();

    private AgeFilter filter = new AgeFilter();

    @Test
    void shouldThrowExceptionWhenBornInTheFuture() {
        String futureDate = LocalDate.now().plusDays(1).format(ISO_LOCAL_DATE);

        Assertions.assertThatThrownBy(() -> filter.apply(futureDate, NO_ARGS))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Date cannot be in the future");
    }

    @Test
    void shouldReturn0DaysWhenBornToday() {
        String today = LocalDate.now().format(ISO_LOCAL_DATE);

        Object age = filter.apply(today, NO_ARGS);
        assertThat(age).isEqualTo("0 days old");
    }

    @Test
    void shouldReturn1DayWhenBornYesterday() {
        String yesterday = LocalDate.now().minusDays(1).format(ISO_LOCAL_DATE);

        Object age = filter.apply(yesterday, NO_ARGS);
        assertThat(age).isEqualTo("1 day old");
    }

    @Test
    void shouldReturn1MonthWhenBorn1MonthAgo() {
        String oneMonth = LocalDate.now().minusMonths(1).format(ISO_LOCAL_DATE);
        Object age = filter.apply(oneMonth, NO_ARGS);
        assertThat(age).isEqualTo("1 month old");
    }

    @Test
    void shouldReturn2MonthsWhenBorn2MonthsAgo() {
        String twoMonths = LocalDate.now().minusMonths(2).format(ISO_LOCAL_DATE);
        Object age = filter.apply(twoMonths, NO_ARGS);
        assertThat(age).isEqualTo("2 months old");
    }

    @Test
    void shouldReturn1YearWhenBorn1YearAgo() {
        String oneYear = LocalDate.now().minusYears(1).format(ISO_LOCAL_DATE);
        Object age = filter.apply(oneYear, NO_ARGS);
        assertThat(age).isEqualTo("1 year old");
    }

    @Test
    void shouldReturn2YearsWhenBorn2YearsAgo() {
        String twoYears = LocalDate.now().minusYears(2).format(ISO_LOCAL_DATE);
        Object age = filter.apply(twoYears, NO_ARGS);
        assertThat(age).isEqualTo("2 years old");
    }
}
