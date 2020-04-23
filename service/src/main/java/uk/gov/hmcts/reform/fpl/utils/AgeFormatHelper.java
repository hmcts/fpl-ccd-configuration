package uk.gov.hmcts.reform.fpl.utils;

import java.time.LocalDate;
import java.time.Period;

import static java.lang.String.format;
import static java.time.LocalDate.now;

public class AgeFormatHelper {
    private AgeFormatHelper() {
    }

    public static String formatAge(final LocalDate dateOfBirth) {
        final Period period = Period.between(dateOfBirth, now());
        return String.join(formattedYears(period.getYears()), formattedMonths(period.getMonths()),
            formattedDays(period.getDays()), " old");
    }

    private static String formattedYears(final int years) {
        if (years > 1) {
            return format("%d years", years);
        } else if (years == 1) {
            return format("%d year", years);
        }
        return "";
    }

    private static String formattedMonths(final int months) {
        if (months > 1) {
            return format("%d months", months);
        } else if (months == 1) {
            return format("%d month", months);
        }
        return "";
    }

    private static String formattedDays(final int days) {
        if (days > 1) {
            return format("%d days", days);
        } else if (days == 1) {
            return format("%d day", days);
        }
        return "";
    }
}
