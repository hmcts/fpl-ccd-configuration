package uk.gov.hmcts.reform.fpl.validation.validators.time;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.HasTimeNotMidnight;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class HasTimeNotMidnightValidator implements ConstraintValidator<HasTimeNotMidnight, LocalDateTime> {
    private static final LocalTime MIDNIGHT = LocalTime.of(0, 0, 0);

    @Override
    public boolean isValid(LocalDateTime localDateTime, ConstraintValidatorContext context) {
        if (localDateTime != null) {
            return !localDateTime.toLocalTime().equals(MIDNIGHT);
        }
        return true;
    }
}
