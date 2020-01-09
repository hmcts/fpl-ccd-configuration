package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.FormatStyle;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class DateFormatterServiceTest {
    private final DateFormatterService dateFormatterService = new DateFormatterService();

    @Test
    void shouldFormatLocalDateInLongFormat() {
        LocalDate date = createDate();
        String formattedDate = dateFormatterService.formatLocalDateToString(date, FormatStyle.LONG);
        assertThat(formattedDate).isEqualTo("1 January 2019");
    }

    @Test
    void shouldFormatLocalDateInMediumFormat() {
        LocalDate date = createDate();
        String formattedDate = dateFormatterService.formatLocalDateToString(date, FormatStyle.MEDIUM);
        assertThat(formattedDate).isEqualTo("1 Jan 2019");
    }

    @Test
    void shouldFormatLocalDateInShortFormat() {
        LocalDate date = createDate();
        String formattedDate = dateFormatterService.formatLocalDateToString(date, FormatStyle.SHORT);
        assertThat(formattedDate).isEqualTo("01/01/2019");
    }

    @Test
    void shouldFormatLocalDateTimeInExpectedFormat() {
        LocalDateTime date = createDateTime();
        String formattedDate = dateFormatterService.formatLocalDateTimeBaseUsingFormat(date, "h:mma, d MMMM yyyy");
        assertThat(formattedDate).isEqualTo("12:00pm, 1 January 2019");
    }

    @ParameterizedTest
    @ValueSource(ints = {11, 12, 13})
    void shouldReturnThSuffixWhenDayIs11thTo13th(int dayOfMonth) {
        String suffix = dateFormatterService.getDayOfMonthSuffix(dayOfMonth);
        assertThat(suffix).isEqualTo("th");
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 6, 7, 8, 9, 10, 14, 15, 16, 17, 18, 19, 20, 24, 25, 26, 27, 28, 29, 30})
    void shouldReturnThSuffixWhenDayDoesNotEndIn1Or2Or3(int dayOfMonth) {
        String suffix = dateFormatterService.getDayOfMonthSuffix(dayOfMonth);
        assertThat(suffix).isEqualTo("th");
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 23})
    void shouldReturnRdSuffixWhenDayEndsIn3ApartFrom13(int dayOfMonth) {
        String suffix = dateFormatterService.getDayOfMonthSuffix(dayOfMonth);
        assertThat(suffix).isEqualTo("rd");
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 22})
    void shouldReturnNdSuffixWhenDayEndsIn2ApartFrom12(int dayOfMonth) {
        String suffix = dateFormatterService.getDayOfMonthSuffix(dayOfMonth);
        assertThat(suffix).isEqualTo("nd");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 21, 31})
    void shouldReturnStSuffixWhenDayEndsIn1ApartFrom11(int dayOfMonth) {
        String suffix = dateFormatterService.getDayOfMonthSuffix(dayOfMonth);
        assertThat(suffix).isEqualTo("st");
    }

    private LocalDate createDate() {
        return LocalDate.of(2019, 1, 1);
    }

    private LocalDateTime createDateTime() {
        return LocalDateTime.of(2019, 1, 1, 12, 0);
    }
}
