package uk.gov.hmcts.reform.fpl.validators.interfaces;

import uk.gov.hmcts.reform.fpl.validators.HasChildrenNameValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { HasChildrenNameValidator.class })
public @interface HasChildName {
    String message() default "Tell us the names of all children in the case";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
