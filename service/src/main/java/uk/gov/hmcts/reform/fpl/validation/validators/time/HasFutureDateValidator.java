package uk.gov.hmcts.reform.fpl.validation.validators.time;

import uk.gov.hmcts.reform.fpl.validation.interfaces.HasFutureDate;

import java.time.LocalDateTime;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasFutureDateValidator implements ConstraintValidator<HasFutureDate, LocalDateTime> {

    private static final LocalDateTime NOW = LocalDateTime.now();

    @Override
    public boolean isValid(LocalDateTime value, ConstraintValidatorContext context) {
        return value.isAfter(NOW);
    }
}
