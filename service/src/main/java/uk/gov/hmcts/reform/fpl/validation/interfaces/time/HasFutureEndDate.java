package uk.gov.hmcts.reform.fpl.validation.interfaces.time;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import uk.gov.hmcts.reform.fpl.validation.validators.time.HasFutureEndDateValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = HasFutureEndDateValidator.class)
public @interface HasFutureEndDate {
    String message() default "End date should be in the future";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
