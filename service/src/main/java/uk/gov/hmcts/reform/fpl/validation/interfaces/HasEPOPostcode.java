package uk.gov.hmcts.reform.fpl.validation.interfaces;

import uk.gov.hmcts.reform.fpl.validation.validators.HasEPOPostcodeValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { HasEPOPostcodeValidator.class })
public @interface HasEPOPostcode {
    String message() default "Enter a postcode for the EPO";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
