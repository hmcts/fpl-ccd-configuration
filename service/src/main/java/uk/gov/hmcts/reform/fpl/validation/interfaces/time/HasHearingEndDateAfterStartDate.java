package uk.gov.hmcts.reform.fpl.validation.interfaces.time;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import uk.gov.hmcts.reform.fpl.validation.validators.time.HasHearingEndDateAfterStartDateValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = HasHearingEndDateAfterStartDateValidator.class)
public @interface HasHearingEndDateAfterStartDate {
    String message() default "The start date cannot be after the end date";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
