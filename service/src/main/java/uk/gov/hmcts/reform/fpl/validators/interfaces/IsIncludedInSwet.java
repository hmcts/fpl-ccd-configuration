package uk.gov.hmcts.reform.fpl.validators.interfaces;

import uk.gov.hmcts.reform.fpl.validators.IsIncludedInSwetValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { IsIncludedInSwetValidator.class })
public @interface IsIncludedInSwet {
    String message() default "Attach the SWET or change the status from 'Included in SWET'.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
