package uk.gov.hmcts.reform.fpl.validation.validators.time;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.HasFutureEndDate;

import java.time.LocalDateTime;
import java.util.Optional;

public class HasFutureEndDateValidator implements ConstraintValidator<HasFutureEndDate, LocalDateTime> {


    @Override
    public boolean isValid(LocalDateTime localDateTime, ConstraintValidatorContext context) {
        return Optional.ofNullable(localDateTime)
            .map(dateTime -> dateTime.isAfter(LocalDateTime.now()))
            .orElse(true);
    }
}
