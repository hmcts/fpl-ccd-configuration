package uk.gov.hmcts.reform.fpl.validation.interfaces;

import uk.gov.hmcts.reform.fpl.validation.validators.HasGenderValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {HasGenderValidator.class})
public @interface HasGender {
    String message() default "Tell us the gender of all children in the case";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
