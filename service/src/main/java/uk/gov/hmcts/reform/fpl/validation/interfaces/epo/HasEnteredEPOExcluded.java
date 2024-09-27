package uk.gov.hmcts.reform.fpl.validation.interfaces.epo;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import uk.gov.hmcts.reform.fpl.validation.validators.epo.HasEnteredEPOExcludedValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { HasEnteredEPOExcludedValidator.class })
public @interface HasEnteredEPOExcluded {
    String message() default "Enter who you want excluded.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
