package uk.gov.hmcts.reform.fpl.validation.interfaces.epo;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import uk.gov.hmcts.reform.fpl.validation.validators.epo.HasEPOAddressValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { HasEPOAddressValidator.class })
public @interface HasEPOAddress {
    String message() default "Enter the postcode and select the address.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
