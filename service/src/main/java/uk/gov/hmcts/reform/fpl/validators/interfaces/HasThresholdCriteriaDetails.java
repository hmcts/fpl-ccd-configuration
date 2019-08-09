package uk.gov.hmcts.reform.fpl.validators.interfaces;

import uk.gov.hmcts.reform.fpl.validators.HasThresholdCriteriaDetailsValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { HasThresholdCriteriaDetailsValidator.class })
public @interface HasThresholdCriteriaDetails {
    String message() default "Enter details of how the case meets the threshold criteria";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
