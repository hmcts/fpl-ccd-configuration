package uk.gov.hmcts.reform.fpl.validation.validators.time;

import uk.gov.hmcts.reform.fpl.validation.interfaces.time.TimeDifference;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.TimeRange;

import java.time.LocalDateTime;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class TimeRangeValidator implements ConstraintValidator<TimeRange, LocalDateTime> {

    private TimeDifference before;
    private TimeDifference after;
    private boolean inclusive;

    @Override
    public void initialize(TimeRange annotation) {
        before = annotation.rangeBefore();
        after = annotation.rangeAfter();
        inclusive = annotation.inclusive();
    }

    @Override
    public boolean isValid(LocalDateTime value, ConstraintValidatorContext context) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime rangeStart = now.minus(before.amount(), before.unit());
        LocalDateTime rangeEnd = now.plus(after.amount(), after.unit());

        if (inclusive) {
            return !value.isBefore(rangeStart) && !value.isAfter(rangeEnd);
        } else {
            return value.isAfter(rangeStart) && value.isBefore(rangeEnd);
        }
    }
}
