package uk.gov.hmcts.reform.fpl.validation.validators.documents;

import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.validation.interfaces.HasAttachedDocument;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.reform.fpl.enums.DocumentStatus.ATTACHED;
import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.hasDocumentStatusOf;
import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.hasDocumentStatusSet;
import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.hasDocumentUploaded;

public class HasAttachedDocumentValidator implements ConstraintValidator<HasAttachedDocument, Document> {
    @Override
    public boolean isValid(Document document, ConstraintValidatorContext constraintValidatorContext) {
        if (!hasDocumentStatusSet(document)) {
            return true;
        } else if (hasDocumentStatusOf(document, ATTACHED)) {
            return hasDocumentUploaded(document);
        }

        return true;
    }
}
