package uk.gov.hmcts.reform.fpl.validators;

import uk.gov.hmcts.reform.fpl.enums.OrderType;
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

        if (caseData.getOrders() != null && caseData.getOrders().getOrderType() != null
            && caseData.getOrders().getOrderType().contains(OrderType.EMERGENCY_PROTECTION_ORDER)) {

            return caseData.getGroundsForEPO() != null && caseData.getGroundsForEPO().getThresholdReason() != null
                && !caseData.getGroundsForEPO().getThresholdReason().contains("");
        } else {
            return true;
        }
    }
}
