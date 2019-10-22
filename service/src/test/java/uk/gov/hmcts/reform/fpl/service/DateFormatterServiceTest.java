package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    private LocalDate createDate() {
        return LocalDate.of(2019, 1, 1);
    }

    private LocalDateTime createDateTime() {
        return LocalDateTime.of(2019, 1, 1, 12, 0);
    }
}
