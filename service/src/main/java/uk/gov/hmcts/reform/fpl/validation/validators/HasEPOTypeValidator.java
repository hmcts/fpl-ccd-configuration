package uk.gov.hmcts.reform.fpl.validation.validators;

import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.validation.interfaces.HasEPOType;

import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.EMERGENCY_PROTECTION_ORDER;

public class HasEPOTypeValidator implements ConstraintValidator<HasEPOType, Orders> {
    @Override
    public boolean isValid(Orders value, ConstraintValidatorContext context) {
        if (orderContainsEPO(value.getOrderType()) && isNull(value.getEpoType())) {
            return false;
        }
        return true;
    }

    private boolean orderContainsEPO(List<OrderType> orderTypes) {
        return orderTypes.contains(EMERGENCY_PROTECTION_ORDER);
    }
}
