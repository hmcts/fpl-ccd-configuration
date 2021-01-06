package uk.gov.hmcts.reform.fpl.validation.interfaces;

import uk.gov.hmcts.reform.fpl.validation.validators.HasEnteredExcludedValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { HasEnteredExcludedValidator.class })
public @interface HasEnteredExcluded {
    String message() default "Enter excluded";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
