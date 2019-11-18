package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

@Service
public class DateFormatterService {
    public String formatLocalDateToString(LocalDate date, FormatStyle style) {
        return date.format(DateTimeFormatter.ofLocalizedDate(style).localizedBy(Locale.UK));
    }

    public String formatLocalDateTimeBaseUsingFormat(LocalDateTime dateTime, String format) {
        return dateTime.format(DateTimeFormatter.ofPattern(format, Locale.UK));
    }
}
