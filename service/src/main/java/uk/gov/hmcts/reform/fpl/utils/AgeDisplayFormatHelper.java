package uk.gov.hmcts.reform.fpl.utils;

import java.time.LocalDate;
import java.time.Period;

import static com.google.common.base.Preconditions.checkNotNull;

public class AgeDisplayFormatHelper {

    private AgeDisplayFormatHelper() {
    }

    public static String formatAgeDisplay(final LocalDate dateOfBirth) {
        checkNotNull(dateOfBirth, "Date of birth value is required");

        final Period period = Period.between(dateOfBirth, LocalDate.now());
        final int years = period.getYears();
        final int months = period.getMonths();
        final int days = period.getDays();

        if (period.isNegative()) {
            return "0 years old";
        }
        if (years > 1) {
            return years + " years old";
        }
        if (years == 1) {
            return years + " year old";
        }
        if (months > 1) {
            return months + " months old";
        }
        if (months == 1) {
            return months + " month old";
        }
        if (days > 1) {
            return days + " days old";
        }
        if (days == 1) {
            return days + " day old";
        }

        return "0 days old";

    }
}
