package uk.gov.hmcts.reform.fpl.validators.interfaces;

import uk.gov.hmcts.reform.fpl.validators.HasDocumentStatusValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { HasDocumentStatusValidator.class })
public @interface HasDocumentStatus {
    String message() default "Tell us the status of all documents including those that you haven't uploaded";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
