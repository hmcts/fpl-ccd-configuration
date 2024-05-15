package uk.gov.hmcts.reform.fpl.validation.interfaces;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import uk.gov.hmcts.reform.fpl.validation.validators.IsStateMigratableValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { IsStateMigratableValidator.class })
public @interface IsStateMigratable {
    String message() default "Final orders have been issued in this case. You must remove the relevant orders before"
        + " changing the case state.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
