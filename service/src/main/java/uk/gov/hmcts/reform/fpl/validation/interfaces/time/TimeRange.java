package uk.gov.hmcts.reform.fpl.validation.interfaces.time;

import uk.gov.hmcts.reform.fpl.validation.validators.time.TimeRangeValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * Indicates that the annotated value should be within a specified range of the current time.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TimeRangeValidator.class)
public @interface TimeRange {
    String message() default "Enter a valid time";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    TimeDifference maxDate() default @TimeDifference(amount = 0, unit = SECONDS);
}
