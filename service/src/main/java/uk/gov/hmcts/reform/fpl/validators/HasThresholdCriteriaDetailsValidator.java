package uk.gov.hmcts.reform.fpl.validators;

import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasThresholdCriteriaDetails;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasThresholdCriteriaDetailsValidator implements
    ConstraintValidator<HasThresholdCriteriaDetails, CaseData> {

    @Override
    public void initialize(HasThresholdCriteriaDetails constraintAnnotation) {

    }

    @Override
    public boolean isValid(CaseData caseData, ConstraintValidatorContext constraintValidatorContext) {
        constraintValidatorContext.buildConstraintViolationWithTemplate(constraintValidatorContext
            .getDefaultConstraintMessageTemplate()).addPropertyNode("groundsForTheApplication")
            .addConstraintViolation();

        if (caseData.getOrders() != null && caseData.getOrders().getOrderType() != null) {
            if (caseData.getOrders().getOrderType().contains(OrderType.EMERGENCY_PROTECTION_ORDER)) {
                if (caseData.getGroundsForEPO() == null
                    || caseData.getGroundsForEPO().getThresholdDetails() == null
                    || caseData.getGroundsForEPO().getThresholdDetails().isEmpty()) {
                    return false;
                } else {
                    return true;
                }
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
}
