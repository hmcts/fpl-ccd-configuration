package uk.gov.hmcts.reform.fpl.validation.validators;

import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.validation.interfaces.HasEPOAddress;

import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static java.util.Objects.isNull;
import static org.apache.logging.log4j.util.Strings.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.PREVENT_REMOVAL;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.EMERGENCY_PROTECTION_ORDER;

public class HasEPOlAddress implements ConstraintValidator<HasEPOAddress, Orders> {
    @Override
    public boolean isValid(Orders value, ConstraintValidatorContext context) {
        if(orderContainsEPO(value.getOrderType()) && epoIsPreventRemoval(value)) {
           if(isEmpty(value.getAddress().getAddressLine1())) {
               return false;
           }
        }

        return true;
    }

    private boolean epoIsPreventRemoval(Orders orders) {
        return !isNull(orders) && orders.getEpoType() == PREVENT_REMOVAL;
    }

    private boolean orderContainsEPO(List<OrderType> orderTypes) {
        return orderTypes.contains(EMERGENCY_PROTECTION_ORDER);
    }
}
