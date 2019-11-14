package uk.gov.hmcts.reform.fpl.validators;

import uk.gov.hmcts.reform.fpl.validators.interfaces.TimeNotZero;

import java.time.LocalDateTime;
import java.time.LocalTime;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class TimeNotZeroValidator implements ConstraintValidator<TimeNotZero, LocalDateTime> {
    private static final LocalTime INVALID_TIME = LocalTime.of(0,0,0);

    @Override
    public boolean isValid(LocalDateTime value, ConstraintValidatorContext context) {
        return !value.toLocalTime().equals(INVALID_TIME);
    }
}
