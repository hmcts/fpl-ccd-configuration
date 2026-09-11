package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.getDayOfMonthSuffix;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.parseLocalDateFromStringUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.parseLocalDateTimeFromStringUsingFormat;

class DateFormatterHelperTest {
    private static final String JANUARY_2019 = "1 January 2019";
    private static final String JANUARY_2019_TIME = "1 January 2019, 12:00pm";
    private static final String TIME_JANUARY_2019 = "12:00pm, 1 January 2019";

    private static Stream<Arguments> dayOfMonthSuffixSource() {
        return Stream.of(
            Arguments.of(new int[] {
                4, 5, 6, 7, 8, 9, 10, 11, 12,
                13, 14, 15, 16, 17, 18, 19, 20,
                24, 25, 26, 27, 28, 29, 30}, "th"),
            Arguments.of(new int[] {1, 21, 31}, "st"),
            Arguments.of(new int[] {2, 22}, "nd"),
            Arguments.of(new int[] {3, 23}, "rd")
        );
    }

    @Test
    void shouldFormatLocalDateInLongFormat() {
        LocalDate date = createDate();
        String formattedDate = formatLocalDateToString(date, FormatStyle.LONG);
        assertThat(formattedDate).isEqualTo(JANUARY_2019);
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
    void shouldFormatLocalDateInMediumFormatWithLocaleWelsh() {
        LocalDate date = createDate();
        String formattedDate = formatLocalDateToString(date, "d MMMM yyyy", Language.WELSH);
        assertThat(formattedDate).isEqualTo("1 Ionawr 2019");
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

    @Test
    void shouldParseAFormattedDateToLocalDateWhenGivenCorrectFormat() {
        LocalDate parsed = parseLocalDateFromStringUsingFormat(JANUARY_2019, DATE);
        assertThat(parsed).isEqualTo(createDate());
    }

    @Test
    void shouldThrowExceptionWhenFormatDoesNotMatchDate() {
        assertThrows(DateTimeParseException.class, () -> parseLocalDateFromStringUsingFormat(JANUARY_2019, "d MM y"));
    }

    @Test
    void shouldThrowExceptionWhenDateIsNull() {
        assertThrows(NullPointerException.class, () -> parseLocalDateFromStringUsingFormat(null, DATE));
    }

    @Test
    void shouldThrowExceptionWhenDateFormatIsNull() {
        assertThrows(NullPointerException.class, () -> parseLocalDateFromStringUsingFormat(JANUARY_2019, null));
    }

    @Test
    void shouldParseAFormattedDateTimeToLocalDateTime() {
        LocalDateTime parsed = parseLocalDateTimeFromStringUsingFormat(JANUARY_2019_TIME, DATE_TIME);
        assertThat(parsed).isEqualTo(createDateTime());
    }

    @Test
    void shouldParseAFormattedDateTimeToLocalDateTimeWhenGivenMainFormat() {
        LocalDateTime parsed = parseLocalDateTimeFromStringUsingFormat(JANUARY_2019_TIME, DATE_TIME, TIME_DATE);
        assertThat(parsed).isEqualTo(createDateTime());
    }

    @Test
    void shouldParseAFormattedDateTimeToLocalDateTimeWhenGivenAlternativeFormat() {
        LocalDateTime parsed = parseLocalDateTimeFromStringUsingFormat(TIME_JANUARY_2019, DATE_TIME, TIME_DATE);
        assertThat(parsed).isEqualTo(createDateTime());
    }

    @Test
    void shouldThrowExceptionWhenDateTimeDoesNotMatchEitherFormat() {
        assertThrows(DateTimeParseException.class,
            () -> parseLocalDateTimeFromStringUsingFormat(JANUARY_2019_TIME, "d MM y", "h:mma, yyyy"));
    }

    @Test
    void shouldThrowExceptionWhenDateTimeIsNull() {
        assertThrows(NullPointerException.class, () -> parseLocalDateTimeFromStringUsingFormat(null, DATE, DATE));
    }

    @Test
    void shouldThrowExceptionWhenBothFormatsAreNull() {
        assertThrows(NullPointerException.class,
            () -> parseLocalDateTimeFromStringUsingFormat(JANUARY_2019_TIME, null, null));
    }

    @Test
    void shouldReturnEmptyIfDateTimeIsNull() {
        assertThat(formatLocalDateTimeBaseUsingFormat(null, "h:mma, yyyy")).isEmpty();
    }

    private LocalDate createDate() {
        return LocalDate.of(2019, 1, 1);
    }

    private LocalDateTime createDateTime() {
        return LocalDateTime.of(2019, 1, 1, 12, 0);
    }
}
