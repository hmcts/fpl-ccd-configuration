package uk.gov.hmcts.reform.pebble;

import com.mitchellbosecke.pebble.extension.Filter;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

public class AgeFilter implements Filter {
    @Override
    public List<String> getArgumentNames() {
        return null;
    }

    @Override
    public Object apply(Object input, Map<String, Object> args) {
        try {
            LocalDate.parse(input.toString());
        } catch (DateTimeParseException exc) {
            throw new IllegalArgumentException("Date is in an incorrect format");
        }

        LocalDate today = LocalDate.now();
        LocalDate inputDate = LocalDate.parse(input.toString());

        if (inputDate.isAfter(today)) {
            throw new IllegalArgumentException("Date cannot be in the future");
        }

        Period period = Period.between(inputDate, today);
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
