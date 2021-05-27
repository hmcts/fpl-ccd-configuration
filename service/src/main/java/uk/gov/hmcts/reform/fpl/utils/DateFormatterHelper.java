package uk.gov.hmcts.reform.fpl.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.Locale;

public class DateFormatterHelper {
    public static final String DATE_TIME_AT = "d MMMM yyyy 'at' h:mma";
    public static final String TIME_DATE = "h:mma, d MMMM yyyy";
    public static final String DATE_TIME = "d MMMM yyyy, h:mma";
    public static final String DATE = "d MMMM yyyy";
    public static final String DATE_SHORT_MONTH = "d MMM yyyy";
    public static final String DATE_TIME_WITH_ORDINAL_SUFFIX = "h:mma 'on the' d'%s' MMMM y";
    public static final String DATE_WITH_ORDINAL_SUFFIX = "d'%s' MMMM y";
    public static final String DATE_MONTH = "ddMMMM";

    private DateFormatterHelper() {
        // NO-OP
    }

    public static String formatLocalDateToString(LocalDate date, FormatStyle style) {
        return date.format(DateTimeFormatter.ofLocalizedDate(style).localizedBy(Locale.UK));
    }

    public static String formatLocalDateToString(LocalDate date, String format) {
        return date.format(DateTimeFormatter.ofPattern(format, Locale.UK));
    }

    public static String formatLocalDateTimeBaseUsingFormat(LocalDateTime dateTime, String format) {
        return dateTime.format(DateTimeFormatter.ofPattern(format, Locale.UK));
    }

    public static LocalDate parseLocalDateFromStringUsingFormat(String date, String format) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern(format, Locale.UK));
    }

    public static LocalDateTime parseLocalDateTimeFromStringUsingFormat(String date, String main, String alternative) {
        try {
            return LocalDateTime.parse(date, DateTimeFormatter.ofPattern(main, Locale.UK));
        } catch (DateTimeParseException e) {
            return LocalDateTime.parse(date, DateTimeFormatter.ofPattern(alternative, Locale.UK));
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
}
