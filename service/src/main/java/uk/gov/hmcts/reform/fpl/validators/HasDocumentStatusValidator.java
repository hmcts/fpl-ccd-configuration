package uk.gov.hmcts.reform.fpl.validators;

import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasDocumentStatus;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasDocumentStatusValidator implements ConstraintValidator<HasDocumentStatus, Document> {
    private static final String ERROR_TEMPLATE = "Remove the document or change the status from";

    @Override
    public boolean isValid(Document document, ConstraintValidatorContext constraintValidatorContext) {
        if (document == null || document.getDocumentStatus() == null) {
            return true;
        } else if (document.getDocumentStatus().equals("Attached")) {
            return document.getTypeOfDocument() != null;
        } else if (document.getDocumentStatus().equals("To follow")
            && document.getTypeOfDocument() != null) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(ERROR_TEMPLATE
                + " 'To follow'.").addConstraintViolation();

            return false;
        } else if (document.getDocumentStatus().equals("Included in social work evidence template (SWET)")
            && document.getTypeOfDocument() != null) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(ERROR_TEMPLATE
                + " 'Included in SWET'.").addConstraintViolation();

            return false;
        }

        return true;
    }
}
