package uk.gov.hmcts.reform.fpl.validators;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasDocumentStatus;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasDocumentStatusValidator implements ConstraintValidator<HasDocumentStatus, CaseData> {
    @Override
    public void initialize(HasDocumentStatus constraintAnnotation) { }

    @Override
    public boolean isValid(CaseData caseData, ConstraintValidatorContext constraintValidatorContext) {
        if (caseData.getDocuments_checklist_document() != null || caseData.getDocuments_socialWorkStatement_document() != null ||
            caseData.getDocuments_socialWorkCarePlan_document() != null || caseData.getDocuments_socialWorkChronology_document() != null ||
            caseData.getDocuments_threshold_document() != null || caseData.getDocuments_socialWorkAssessment_document() != null) {


            if (validateStatus(caseData.getDocuments_checklist_document()) &&
                validateStatus(caseData.getDocuments_socialWorkCarePlan_document()) &&
                validateStatus(caseData.getDocuments_socialWorkStatement_document()) &&
                validateStatus(caseData.getDocuments_socialWorkChronology_document()) &&
                validateStatus(caseData.getDocuments_threshold_document()) &&
                validateStatus(caseData.getDocuments_socialWorkAssessment_document())) {
                return true;
            } else {
                return false;
            }

        } else {
            return false;
        }
    }

    private Boolean validateStatus(Document document) {
        Boolean isValid = true;

        if (document == null || document.getDocumentStatus() == null || document.getDocumentStatus().isBlank()) {
            isValid = false;
        }

        return isValid;
    }
}
