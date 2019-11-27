package uk.gov.hmcts.reform.fpl.validation.interfaces;

import uk.gov.hmcts.reform.fpl.validation.validators.documents.HasAttachedDocumentValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { HasAttachedDocumentValidator.class })
public @interface HasAttachedDocument {
    String message() default "Attach the document or change the status from 'Attached'.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
