package uk.gov.hmcts.reform.fpl.validators.interfaces;

import uk.gov.hmcts.reform.fpl.validators.HasMainApplicantValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { HasMainApplicantValidator.class })
public @interface HasMainApplicant {
    String message() default "You need to add details to applicant";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
