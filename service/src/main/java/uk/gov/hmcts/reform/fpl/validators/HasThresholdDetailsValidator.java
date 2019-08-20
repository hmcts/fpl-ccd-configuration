package uk.gov.hmcts.reform.fpl.validators;

import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasThresholdDetails;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasThresholdDetailsValidator implements
    ConstraintValidator<HasThresholdDetails, CaseData> {
    @Override
    public boolean isValid(CaseData caseData, ConstraintValidatorContext constraintValidatorContext) {
        if (caseData.getOrders() != null && caseData.getOrders().getOrderType() != null
            && caseData.getOrders().getOrderType().contains(OrderType.EMERGENCY_PROTECTION_ORDER)) {
            constraintValidatorContext.buildConstraintViolationWithTemplate(constraintValidatorContext
                .getDefaultConstraintMessageTemplate()).addPropertyNode("groundsForTheApplication")
                .addConstraintViolation();

            return caseData.getGrounds() != null
                && StringUtils.isNotBlank(caseData.getGrounds().getThresholdDetails());
        } else {
            return true;
        }
    }
}
