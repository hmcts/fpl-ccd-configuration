package uk.gov.hmcts.reform.fpl.validation.validators.documents;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.validation.interfaces.HasDocumentsIncludedInSwet;
import uk.gov.hmcts.reform.fpl.validation.models.ValidationDocumentMap;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.DocumentStatus.ATTACHED;
import static uk.gov.hmcts.reform.fpl.enums.DocumentStatus.INCLUDED_IN_SWET;
import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.hasDocumentStatusOf;
import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.hasDocumentUploaded;

public class HasDocumentsIncludedInSwetValidator implements ConstraintValidator<HasDocumentsIncludedInSwet, CaseData> {
    @Override
    public boolean isValid(CaseData caseData, ConstraintValidatorContext constraintValidatorContext) {
        List<ValidationDocumentMap> validationDocumentBundle = getDocumentValidationBundle(caseData);
        boolean isValid = true;

        for (ValidationDocumentMap validationDocumentMap : validationDocumentBundle) {
            if (hasDocumentStatusOf(validationDocumentMap.getDocument(), INCLUDED_IN_SWET)
                && !hasAttachedSwet(caseData.getSocialWorkEvidenceTemplateDocument())) {
                setViolationMessage(constraintValidatorContext, validationDocumentMap);
                isValid = false;
            }
        }

        return isValid;
    }

    private void setViolationMessage(ConstraintValidatorContext constraintValidatorContext,
                                     ValidationDocumentMap mappedDocument) {
        constraintValidatorContext.buildConstraintViolationWithTemplate(constraintValidatorContext
            .getDefaultConstraintMessageTemplate())
            .addPropertyNode(mappedDocument.getKey()).addConstraintViolation();
        constraintValidatorContext.disableDefaultConstraintViolation();
    }

    private boolean hasAttachedSwet(Document socialWorkEvidence) {
        return hasDocumentStatusOf(socialWorkEvidence, ATTACHED) && hasDocumentUploaded(socialWorkEvidence);
    }

    private List<ValidationDocumentMap> getDocumentValidationBundle(CaseData caseData) {
        List<ValidationDocumentMap> documents = new ArrayList<>();

        documents.add(createValidationDocumentMap("socialWorkCarePlanDocument",
            caseData.getSocialWorkCarePlanDocument()));
        documents.add(createValidationDocumentMap("socialWorkStatementDocument",
            caseData.getSocialWorkStatementDocument()));
        documents.add(createValidationDocumentMap("socialWorkAssessmentDocument",
            caseData.getSocialWorkAssessmentDocument()));
        documents.add(createValidationDocumentMap("socialWorkChronologyDocument",
            caseData.getSocialWorkChronologyDocument()));
        documents.add(createValidationDocumentMap("checklistDocument", caseData.getChecklistDocument()));
        documents.add(createValidationDocumentMap("thresholdDocument", caseData.getThresholdDocument()));

        return documents;
    }

    private ValidationDocumentMap createValidationDocumentMap(String key, Document document) {
        return ValidationDocumentMap.builder().key(key).document(document).build();
    }
}
