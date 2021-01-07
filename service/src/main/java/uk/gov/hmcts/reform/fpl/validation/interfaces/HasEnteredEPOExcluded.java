package uk.gov.hmcts.reform.fpl.validation.interfaces;

import uk.gov.hmcts.reform.fpl.validation.validators.HasEnteredEPOExcludedValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { HasEnteredEPOExcludedValidator.class })
public @interface HasEnteredEPOExcluded {
    String message() default "Enter who you want excluded.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
