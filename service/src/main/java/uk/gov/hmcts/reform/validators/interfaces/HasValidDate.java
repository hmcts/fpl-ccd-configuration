package uk.gov.hmcts.reform.validators.interfaces;

import uk.gov.hmcts.reform.validators.HasValidDateValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { HasValidDateValidator.class })
public @interface HasValidDate {
    String message() default "Enter a future date";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
