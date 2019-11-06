package uk.gov.hmcts.reform.fpl.validators;

import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasAttachedDocument;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasAttachedDocumentValidator implements ConstraintValidator<HasAttachedDocument, Document> {
    @Override
    public boolean isValid(Document document, ConstraintValidatorContext constraintValidatorContext) {
        if (document == null || document.getDocumentStatus() == null) {
            return true;
        } else if (document.getDocumentStatus().equals("Attached")) {
            return document.getTypeOfDocument() != null;
        }

        return true;
    }
}
