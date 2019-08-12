package uk.gov.hmcts.reform.fpl.validators.interfaces;

import uk.gov.hmcts.reform.fpl.validators.HasEPOGroundsValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { HasEPOGroundsValidator.class })
public @interface HasEPOGrounds {
    String message() default "Select at least one option for how this case meets grounds for an emergency protection"
        + " order";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
