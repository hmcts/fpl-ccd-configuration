package uk.gov.hmcts.reform.fpl.validators;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasDocuments;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasDocumentsValidator implements ConstraintValidator<HasDocuments, CaseData> {
    @Override
    public void initialize(HasDocuments constraintAnnotation) { }

    @Override
    public boolean isValid(CaseData caseData, ConstraintValidatorContext constraintValidatorContext) {

        if (caseData.getDocuments_checklist_document() != null || caseData.getDocuments_socialWorkStatement_document() != null ||
            caseData.getDocuments_socialWorkCarePlan_document() != null || caseData.getDocuments_socialWorkChronology_document() != null ||
            caseData.getDocuments_threshold_document() != null ) {
            return true;
        }

        return false;
    }
}
