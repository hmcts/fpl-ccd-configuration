package uk.gov.hmcts.reform.fpl.validation.interfaces;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import uk.gov.hmcts.reform.fpl.validation.validators.documents.HasDocumentsIncludedInSwetValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { HasDocumentsIncludedInSwetValidator.class })
public @interface HasDocumentsIncludedInSwet {
    String message() default "Attach the SWET or change the status from 'Included in SWET'.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
