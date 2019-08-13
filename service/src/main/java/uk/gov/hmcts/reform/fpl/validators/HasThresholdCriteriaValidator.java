package uk.gov.hmcts.reform.fpl.validators;

import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasThresholdCriteria;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasThresholdCriteriaValidator implements ConstraintValidator<HasThresholdCriteria, CaseData> {
    @Override
    public void initialize(HasThresholdCriteria constraintAnnotation) {

    }

    @Override
    public boolean isValid(CaseData caseData, ConstraintValidatorContext constraintValidatorContext) {

        if (caseData.getOrders() != null && caseData.getOrders().getOrderType() != null) {
            constraintValidatorContext.buildConstraintViolationWithTemplate(constraintValidatorContext
                .getDefaultConstraintMessageTemplate()).addPropertyNode("groundsForTheApplication").addConstraintViolation();

            if (caseData.getOrders().getOrderType().contains(OrderType.EMERGENCY_PROTECTION_ORDER)) {
                if (caseData.getGroundsForEPO() == null
                    || caseData.getGroundsForEPO().getThresholdReason() == null
                    || caseData.getGroundsForEPO().getThresholdReason().contains("")) {
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
