package uk.gov.hmcts.reform.fpl.validators;

import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasThresholdCriteriaDetails;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasThresholdCriteriaDetailsValidator implements ConstraintValidator<HasThresholdCriteriaDetails, CaseData> {
    @Override
    public void initialize(HasThresholdCriteriaDetails constraintAnnotation) { }

    @Override
    public boolean isValid(CaseData caseData, ConstraintValidatorContext constraintValidatorContext) {
        if (caseData.getOrders() != null && caseData.getOrders().getOrderType() != null &&
            caseData.getOrders().getOrderType().contains(OrderType.EMERGENCY_PROTECTION_ORDER) &&
            caseData.getGroundsForEPO() != null &&
            caseData.getGroundsForEPO().getThresholdDetails() != null) {
            return true;
        } else {
            return false;
        }
    }
}
