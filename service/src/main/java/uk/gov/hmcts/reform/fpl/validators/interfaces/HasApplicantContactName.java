package uk.gov.hmcts.reform.fpl.validators.interfaces;

import uk.gov.hmcts.reform.fpl.validators.HasApplicantContactNameValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { HasApplicantContactNameValidator.class })
public @interface HasApplicantContactName {
    String message() default "Enter the contact's full name";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
