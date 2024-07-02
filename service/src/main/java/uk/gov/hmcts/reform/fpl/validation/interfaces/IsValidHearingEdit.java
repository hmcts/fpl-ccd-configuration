package uk.gov.hmcts.reform.fpl.validation.interfaces;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import uk.gov.hmcts.reform.fpl.validation.validators.IsValidHearingEditValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { IsValidHearingEditValidator.class })
public @interface IsValidHearingEdit {
    String message() default "There are no relevant hearings to change.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
