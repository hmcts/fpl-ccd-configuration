package uk.gov.hmcts.reform.fpl.validation.interfaces;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import uk.gov.hmcts.reform.fpl.validation.validators.HasContactDirectionValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { HasContactDirectionValidator.class })
public @interface HasContactDirection {
    String message() default "Enter the contact's full name";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
