package uk.gov.hmcts.reform.fpl.validation.interfaces.time;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import uk.gov.hmcts.reform.fpl.validation.validators.time.HasTimeNotMidnightValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = HasTimeNotMidnightValidator.class)
public @interface HasTimeNotMidnight {
    // REFACTOR: 21/11/2019 This could be changed to HasValidTime.
    //  New property/properties could be added to store the (in?)valid time/time range.
    //  These new properties can then be used in the validator.
    String message() default "Enter a valid time";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
