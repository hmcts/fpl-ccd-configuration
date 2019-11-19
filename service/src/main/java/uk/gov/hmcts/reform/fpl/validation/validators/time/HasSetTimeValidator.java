package uk.gov.hmcts.reform.fpl.validation.validators.time;

import uk.gov.hmcts.reform.fpl.validation.interfaces.time.HasSetTime;

import java.time.LocalDateTime;
import java.time.LocalTime;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasSetTimeValidator implements ConstraintValidator<HasSetTime, LocalDateTime> {
    private static final LocalTime INVALID_TIME = LocalTime.of(0, 0, 0);

    @Override
    public boolean isValid(LocalDateTime localDateTime, ConstraintValidatorContext context) {
        return !localDateTime.toLocalTime().equals(INVALID_TIME);
    }
}
