package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.fpl.model.configuration.Language;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.time.format.ResolverStyle;
import java.util.Locale;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class DateFormatterHelper {
    public static final String DATE_TIME_AT = "d MMMM yyyy 'at' h:mma";
    public static final String TIME_DATE = "h:mma, d MMMM yyyy";
    public static final String DATE_TIME = "d MMMM yyyy, h:mma";
    public static final String DATE = "d MMMM yyyy";
    public static final String DATE_TIME_WITH_ORDINAL_SUFFIX = "h:mma 'on the' d'%s' MMMM y";
    public static final String DATE_WITH_ORDINAL_SUFFIX = "d'%s' MMMM y";
    public static final String DATE_SHORT = "dd/MM/yyyy";

    private DateFormatterHelper() {
        // NO-OP
    }

    public static String formatLocalDateToString(LocalDate date, FormatStyle style) {
        return date.format(DateTimeFormatter.ofLocalizedDate(style).localizedBy(Locale.UK));
    }

    public static String formatLocalDateToString(LocalDate date, String format) {
        return date.format(DateTimeFormatter.ofPattern(format, Locale.UK));
    }

    public static String formatLocalDateToString(LocalDate date, String format, Language language) {
        return date.format(DateTimeFormatter.ofPattern(format, language.getLocale()));
    }

    public static String formatLocalDateTimeBaseUsingFormat(LocalDateTime dateTime, String format) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DateTimeFormatter.ofPattern(format, Locale.UK));
    }

    public static String formatLocalDateBaseUsingFormat(LocalDate date, String format) {
        return date.format(DateTimeFormatter.ofPattern(format, Locale.UK));
    }

    public static LocalDate parseLocalDateFromStringUsingFormat(String date, String format) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern(format, Locale.UK));
    }

    public static LocalDateTime parseLocalDateTimeFromStringUsingFormat(String date, String format) {
        return LocalDateTime.parse(date, DateTimeFormatter.ofPattern(format, Locale.UK));
    }

    public static LocalDateTime parseLocalDateTimeFromStringUsingFormat(String date, String main, String alternative) {
        try {
            return parseLocalDateTimeFromStringUsingFormat(date, main);
        } catch (DateTimeParseException e) {
            return parseLocalDateTimeFromStringUsingFormat(date, alternative);
        }
    }

    public static String getDayOfMonthSuffix(int day) {
        if (day <= 0 || day >= 32) {
            throw new IllegalArgumentException("Illegal day of month: " + day);
        }

        if (day >= 11 && day <= 13) {
            return "th";
        }

        switch (day % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    public static boolean isValidDate(String date) {
        return isValidDate(date, DATE_SHORT,
            "d/M/yyyy",
            "d-MM-yyyy",
            "dd-MM-yyyy",
            "d.MM.yyyy",
            "dd.MM.yyyy",
            "yyyy-MM-dd",
            "yyyy/MM/dd",
            "dd MMM yyyy",
            DATE);
    }

    public static boolean isValidDate(String date, String... formats) {
        if (isEmpty(date)) {
            return false;
        }

        for (String format : formats) {
            if (!isEmpty(format)) {
                try {
                    parseLocalDateFromStringUsingFormat(date, format);
                    return true;
                } catch (DateTimeParseException e) {
                    // Try next pattern.
                }
            }
        }

        return false;
    }
}
