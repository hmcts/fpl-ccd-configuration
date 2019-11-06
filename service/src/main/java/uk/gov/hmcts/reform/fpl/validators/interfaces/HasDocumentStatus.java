package uk.gov.hmcts.reform.fpl.validators.interfaces;

import uk.gov.hmcts.reform.fpl.validators.HasDocumentStatusValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { HasDocumentStatusValidator.class })
public @interface HasDocumentStatus {
    String message() default "Attach the document or change the status from 'Attached'.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
