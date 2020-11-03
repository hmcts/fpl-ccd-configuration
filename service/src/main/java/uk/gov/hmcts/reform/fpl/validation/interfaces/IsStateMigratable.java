package uk.gov.hmcts.reform.fpl.validation.interfaces;

import uk.gov.hmcts.reform.fpl.validation.validators.IsStateMigratableValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { IsStateMigratableValidator.class })
public @interface IsStateMigratable {
    String message() default "Final orders have been issued in this case. You must remove the relevant orders before"
        + " changing the case state.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
