package uk.gov.hmcts.reform.fpl.validators;

import org.apache.commons.lang.StringUtils;
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

        if (caseData.checklistDocument() != null
            || caseData.socialWorkStatementDocument() != null
            || caseData.socialWorkCarePlanDocument() != null
            || caseData.socialWorkChronologyDocument() != null
            || caseData.thresholdDocument() != null
            || caseData.socialWorkAssessmentDocument() != null) {

            return validateStatus(caseData.checklistDocument())
                && validateStatus(caseData.socialWorkCarePlanDocument())
                && validateStatus(caseData.socialWorkStatementDocument())
                && validateStatus(caseData.socialWorkChronologyDocument())
                && validateStatus(caseData.thresholdDocument())
                && validateStatus(caseData.socialWorkAssessmentDocument());
        } else {
            return false;
        }
    }

    private Boolean validateStatus(Document document) {
        return document != null && StringUtils.isNotBlank(document.getDocumentStatus());
    }
}
