package uk.gov.hmcts.reform.fpl.validators.interfaces;

import uk.gov.hmcts.reform.fpl.validators.HasThresholdReasonValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { HasThresholdReasonValidator.class })
public @interface HasThresholdReason {
    String message() default "Select at least one option for how this case meets the threshold criteria";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
