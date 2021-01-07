package uk.gov.hmcts.reform.fpl.validation.validators;

import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.validation.interfaces.HasEPOAddress;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static org.apache.logging.log4j.util.Strings.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.PREVENT_REMOVAL;

public class HasEPOAddressValidator implements ConstraintValidator<HasEPOAddress, Orders> {

    @Override
    public boolean isValid(Orders value, ConstraintValidatorContext context) {
        if (value.orderContainsEPO() && epoIsPreventRemoval(value)) {
            if (isEmpty(value.getAddress().getAddressLine1()) || isEmpty(value.getAddress().getPostcode())) {
                return false;
            }
        }
        return true;
    }

    private boolean epoIsPreventRemoval(Orders orders) {
        return orders.getEpoType() == PREVENT_REMOVAL;
    }
}
