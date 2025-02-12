package uk.gov.hmcts.reform.fpl.validation.interfaces.epo;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import uk.gov.hmcts.reform.fpl.validation.validators.epo.HasEPOTypeValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { HasEPOTypeValidator.class })
public @interface HasEPOType {
    String message() default "Select the type of EPO you need.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
