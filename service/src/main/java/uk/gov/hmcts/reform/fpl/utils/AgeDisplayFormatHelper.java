package uk.gov.hmcts.reform.fpl.utils;

import java.time.LocalDate;
import java.time.Period;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class AgeDisplayFormatHelper {

    private AgeDisplayFormatHelper() {
    }

    public static String formatAgeDisplay(final LocalDate dateOfBirth) {
        checkNotNull(dateOfBirth, "Date of birth value is required");
        checkArgument(!dateOfBirth.isAfter(LocalDate.now()), "Date of birth cannot be in future");

        Period period = Period.between(dateOfBirth, LocalDate.now());
        int years = period.getYears();
        int months = period.getMonths();
        int days = period.getDays();

        if (years > 1) {
            return years + " years old";
        } else if (years == 1) {
            return years + " year old";
        } else if (months > 1) {
            return months + " months old";
        } else if (months == 1) {
            return months + " month old";
        } else if (days > 1) {
            return days + " days old";
        } else if (days == 1) {
            return days + " day old";
        } else {
            return "0 days old";
        }
    }
}
