package uk.gov.hmcts.reform.fpl.validators;

import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasEPOGrounds;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasEPOGroundsValidator implements ConstraintValidator<HasEPOGrounds, CaseData> {
    @Override
    public void initialize(HasEPOGrounds constraintAnnotation) {

    }

    @Override
    public boolean isValid(CaseData caseData, ConstraintValidatorContext constraintValidatorContext) {
        constraintValidatorContext.buildConstraintViolationWithTemplate(constraintValidatorContext
            .getDefaultConstraintMessageTemplate()).addPropertyNode("groundsForTheApplication").addConstraintViolation();

        if (caseData.getOrders() != null && caseData.getOrders().getOrderType() != null) {

            if (caseData.getOrders().getOrderType().contains(OrderType.EMERGENCY_PROTECTION_ORDER)) {
                if (caseData.getGroundsForEPO() == null
                    || caseData.getGroundsForEPO().getReason() == null
                    || caseData.getGroundsForEPO().getReason().contains("")) {
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
