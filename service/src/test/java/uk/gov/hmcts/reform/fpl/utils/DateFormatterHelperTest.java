package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.getDayOfMonthSuffix;

@ExtendWith(SpringExtension.class)
class DateFormatterHelperTest {

    private static Stream<Arguments> dayOfMonthSuffixSource() {
        return Stream.of(
            Arguments.of(new int[]{
                4, 5, 6, 7, 8, 9, 10, 11, 12,
                13, 14, 15, 16, 17, 18, 19, 20,
                24, 25, 26, 27, 28, 29, 30}, "th"),
            Arguments.of(new int[]{1, 21, 31}, "st"),
            Arguments.of(new int[]{2, 22}, "nd"),
            Arguments.of(new int[]{3, 23}, "rd")
        );
    }

    @Test
    void shouldFormatLocalDateInLongFormat() {
        LocalDate date = createDate();
        String formattedDate = formatLocalDateToString(date, FormatStyle.LONG);
        assertThat(formattedDate).isEqualTo("1 January 2019");
    }

    @Test
    void shouldFormatLocalDateInMediumFormat() {
        LocalDate date = createDate();
        String formattedDate = formatLocalDateToString(date, FormatStyle.MEDIUM);
        assertThat(formattedDate).isEqualTo("1 Jan 2019");
    }

    @Test
    void shouldFormatLocalDateInShortFormat() {
        LocalDate date = createDate();
        String formattedDate = formatLocalDateToString(date, FormatStyle.SHORT);
        assertThat(formattedDate).isEqualTo("01/01/2019");
    }

    @Test
    void shouldFormatLocalDateTimeInExpectedFormat() {
        LocalDateTime date = createDateTime();
        String formattedDate = formatLocalDateTimeBaseUsingFormat(date, "h:mma, d MMMM yyyy");
        assertThat(formattedDate).isEqualTo("12:00pm, 1 January 2019");
    }

    @ParameterizedTest
    @MethodSource(value = "dayOfMonthSuffixSource")
    void shouldReturnExpectedSuffixWhenGivenAValidDay(int[] days, String expected) {
        for (int day : days) {
            String suffix = getDayOfMonthSuffix(day);
            assertThat(suffix).isEqualTo(expected);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 32})
    void shouldThrowErrorWhenDayOfMonthIsInvalid(int day) {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> getDayOfMonthSuffix(day));

        assertThat(exception.getMessage()).isEqualTo("Illegal day of month: " + day);
    }

    private LocalDate createDate() {
        return LocalDate.of(2019, 1, 1);
    }

    private LocalDateTime createDateTime() {
        return LocalDateTime.of(2019, 1, 1, 12, 0);
    }
}
