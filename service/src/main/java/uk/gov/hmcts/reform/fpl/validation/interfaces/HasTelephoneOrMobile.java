package uk.gov.hmcts.reform.fpl.validation.interfaces;

import uk.gov.hmcts.reform.fpl.validation.validators.HasTelephoneOrMobileValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { HasTelephoneOrMobileValidator.class })
public @interface HasTelephoneOrMobile {
    String message() default "Enter at least one telephone number for the contact";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
