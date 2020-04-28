package uk.gov.hmcts.reform.fpl.utils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;

import static com.google.common.base.Preconditions.checkNotNull;

public class AgeDisplayFormatHelper {

    private AgeDisplayFormatHelper() {
    }

    public static String formatAgeDisplay(final LocalDate dateOfBirth) {
        checkNotNull(dateOfBirth, "Date of birth value is required");

        LocalDate today = LocalDate.now(Clock.systemDefaultZone());

        if (dateOfBirth.isAfter(today)) {
            throw new IllegalArgumentException("Date of birth cannot be in the future");
        }

        Period period = Period.between(dateOfBirth, today);
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
