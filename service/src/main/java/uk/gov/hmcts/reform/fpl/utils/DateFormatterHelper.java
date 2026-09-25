package uk.gov.hmcts.reform.fpl.utils;

import org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.trim;

public class DateFormatterHelper {
    public static final String DATE_TIME_AT = "d MMMM yyyy 'at' h:mma";
    public static final String TIME_DATE = "h:mma, d MMMM yyyy";
    public static final String DATE_TIME = "d MMMM yyyy, h:mma";
    public static final String DATE = "d MMMM yyyy";
    public static final String DATE_TIME_WITH_ORDINAL_SUFFIX = "h:mma 'on the' d'%s' MMMM y";
    public static final String DATE_WITH_ORDINAL_SUFFIX = "d'%s' MMMM y";
    public static final String DATE_SHORT = "dd/MM/yyyy";
    public static final List<String> POSSIBLE_FREETEXT_DATE_FORMATS = List.of(
        DATE_SHORT, "d/MM/yyyy", "dd/M/yyyy", "d/M/yyyy",
        "yyyy/MM/dd", "yyyy/MM/d", "yyyy/M/dd", "yyyy/M/d",
        "dd/MMM/yyyy", "d/MMM/yyyy", "yyyy/MMM/dd", "yyyy/MMM/d",
        "dd/MMMM/yyyy", "d/MMMM/yyyy", "yyyy/MMMM/dd", "yyyy/MMMM/d");
    public static final List<Character> POSSIBLE_FREETEXT_DATE_ANY_OTHER_SYMBOL = List.of('-','.', ' ', '|', '\\');
    public static final List<String> DAY_OF_MONTH_SUFFIXES = List.of("st", "nd", "rd", "th");

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

    public static Optional<LocalDate> parseLocalDateFromStringIfAnyFormatMatches(String date) {
        // This helper method was implemented for DFPL-2423 migration,
        // but it may be also useful for any other free text date parsing scenarios in the future.
        // But it just a quick work for migration, so don't have brain to optimize it, just dump whatever I can think of
        if (!isEmpty(date)) {
            String adjustedDateStr = trim(date);
            for (char symbolChar : POSSIBLE_FREETEXT_DATE_ANY_OTHER_SYMBOL) {
                adjustedDateStr = adjustedDateStr.replace(symbolChar, '/');
            }

            // remove day of month suffix if any
            for (String dayOfMonthSuffix : DAY_OF_MONTH_SUFFIXES) {
                adjustedDateStr = adjustedDateStr.replaceAll("/+" + dayOfMonthSuffix, dayOfMonthSuffix);
            }

            List<String> dateParts = Arrays.stream(adjustedDateStr.split("/"))
                .filter(ObjectUtils::isNotEmpty)
                .collect(Collectors.toList());

            if (dateParts.size() != 3) {
                return Optional.empty();
            }

            // remove day of month suffix if any
            for (String dayOfMonthSuffix : DAY_OF_MONTH_SUFFIXES) {
                dateParts.set(0, dateParts.get(0).replace(dayOfMonthSuffix, ""));
                dateParts.set(2, dateParts.get(2).replace(dayOfMonthSuffix, ""));
            }
            adjustedDateStr = dateParts.get(0) + "/" + dateParts.get(1) + "/" + dateParts.get(2);

            for (String format : POSSIBLE_FREETEXT_DATE_FORMATS) {
                if (!isEmpty(format)) {
                    try {
                        return Optional.of(parseLocalDateFromStringUsingFormat(adjustedDateStr, format));
                    } catch (DateTimeParseException e) {
                        // Try next pattern.
                    }
                }
            }
        }
        return Optional.empty();
    }
}
