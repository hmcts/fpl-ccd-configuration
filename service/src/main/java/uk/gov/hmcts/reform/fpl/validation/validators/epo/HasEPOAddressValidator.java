package uk.gov.hmcts.reform.fpl.validation.validators.epo;

import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.validation.interfaces.epo.HasEPOAddress;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static org.apache.logging.log4j.util.Strings.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.EPOType.PREVENT_REMOVAL;

public class HasEPOAddressValidator implements ConstraintValidator<HasEPOAddress, Orders> {

    @Override
    public boolean isValid(Orders value, ConstraintValidatorContext context) {
        if (value.orderContainsEPO() && epoIsPreventRemoval(value)) {
            return hasPopulatedAddress(value.getAddress());
        }
        return true;
    }

    private boolean hasPopulatedAddress(Address address) {
        return address != null && !isEmpty(address.getAddressLine1()) && !isEmpty(address.getPostcode());
    }

    private boolean epoIsPreventRemoval(Orders orders) {
        return orders.getEpoType() == PREVENT_REMOVAL;
    }
}
