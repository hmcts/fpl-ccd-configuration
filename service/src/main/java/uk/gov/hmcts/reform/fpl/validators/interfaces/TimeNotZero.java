package uk.gov.hmcts.reform.fpl.validators.interfaces;

import uk.gov.hmcts.reform.fpl.validators.TimeNotZeroValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TimeNotZeroValidator.class)
public @interface TimeNotZero {
    String message() default "Enter a valid time";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
