package uk.gov.hmcts.reform.pebble;

import com.google.common.collect.ImmutableList;
import com.mitchellbosecke.pebble.extension.Filter;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class AgeFilter implements Filter {

    private final Clock clock;

    public AgeFilter() {
        this(Clock.systemDefaultZone());
    }

    public AgeFilter(Clock clock) {
        this.clock = clock;
    }

    @Override
    public List<String> getArgumentNames() {
        return ImmutableList.<String>builder().build();
    }

    @Override
    public Object apply(Object input, Map<String, Object> args) {
        checkNotNull(input, "Input value is required");
        checkArgument(input instanceof String, "Input value must be string formatted date");

        LocalDate inputDate;
        try {
            inputDate = LocalDate.parse(input.toString());
        } catch (DateTimeParseException exc) {
            throw new IllegalArgumentException("Input date is in an incorrect format");
        }

        LocalDate today = LocalDate.now(clock);

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
