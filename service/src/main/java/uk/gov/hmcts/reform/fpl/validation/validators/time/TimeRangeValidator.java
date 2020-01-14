package uk.gov.hmcts.reform.fpl.validation.validators.time;

import uk.gov.hmcts.reform.fpl.validation.interfaces.time.TimeDifference;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.TimeRange;

import java.time.LocalDateTime;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class TimeRangeValidator implements ConstraintValidator<TimeRange, LocalDateTime> {

    private TimeDifference after;

    @Override
    public void initialize(TimeRange annotation) {
        after = annotation.rangeAfter();
    }

    @Override
    public boolean isValid(LocalDateTime value, ConstraintValidatorContext context) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime rangeEnd = now.plus(after.amount(), after.unit());

        return !value.isAfter(rangeEnd) || value.isEqual(now);
    }
}
