package uk.gov.hmcts.reform.fpl.validation.validators.time;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.TimeDifference;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.TimeRange;

import java.time.LocalDateTime;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class TimeRangeValidator implements ConstraintValidator<TimeRange, LocalDateTime> {

    private TimeDifference maxDate;
    private final Time time;

    @Autowired
    public TimeRangeValidator(Time time) {
        this.time = time;
    }

    @Override
    public void initialize(TimeRange annotation) {
        maxDate = annotation.maxDate();
    }

    @Override
    public boolean isValid(LocalDateTime value, ConstraintValidatorContext context) {
        LocalDateTime now = time.now();
        LocalDateTime rangeEnd = now.plus(maxDate.amount(), maxDate.unit());

        return !value.isAfter(rangeEnd);
    }
}
