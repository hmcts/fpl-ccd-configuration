package uk.gov.hmcts.reform.fpl.validation.interfaces;

import uk.gov.hmcts.reform.fpl.validation.validators.IsValidEmailAddressValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { IsValidEmailAddressValidator.class })
public @interface IsValidEmailAddress {
    String message() default "Enter a valid email address";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
