package uk.gov.hmcts.reform.fpl.validators;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasThresholdReason;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasThresholdReasonValidator implements ConstraintValidator<HasThresholdReason, CaseData> {
    @Override
    public void initialize(HasThresholdReason constraintAnnotation) {
    }

    @Override
    public boolean isValid(CaseData caseData, ConstraintValidatorContext constraintValidatorContext) {
        constraintValidatorContext.buildConstraintViolationWithTemplate(constraintValidatorContext
            .getDefaultConstraintMessageTemplate()).addPropertyNode("groundsForTheApplication")
            .addConstraintViolation();

        if (caseData.hasEPOGrounds()) {

            return caseData.getGroundsForEPO() != null && caseData.getGroundsForEPO().getThresholdReason() != null
                && !caseData.getGroundsForEPO().getThresholdReason().contains("");
        } else {
            return true;
        }
    }
}
