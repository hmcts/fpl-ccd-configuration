package uk.gov.hmcts.reform.fpl.validators;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasDocumentStatus;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasDocumentStatusValidator implements ConstraintValidator<HasDocumentStatus, CaseData> {
    @Override
    public void initialize(HasDocumentStatus constraintAnnotation) {

    }

    @Override
    public boolean isValid(CaseData caseData, ConstraintValidatorContext constraintValidatorContext) {
        constraintValidatorContext.buildConstraintViolationWithTemplate(constraintValidatorContext
            .getDefaultConstraintMessageTemplate()).addPropertyNode("documents").addConstraintViolation();
        if (caseData.checklist() != null
            || caseData.socialWorkStatement() != null
            || caseData.socialWorkCarePlan() != null
            || caseData.socialWorkChronology() != null
            || caseData.threshold() != null
            || caseData.socialWorkAssessment() != null) {

            if (validateStatus(caseData.checklist())
                && validateStatus(caseData.socialWorkCarePlan())
                && validateStatus(caseData.socialWorkStatement())
                && validateStatus(caseData.socialWorkChronology())
                && validateStatus(caseData.threshold())
                && validateStatus(caseData.socialWorkAssessment())) {
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
