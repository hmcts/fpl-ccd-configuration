package uk.gov.hmcts.reform.fpl.validation.validators.epo;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.validation.interfaces.epo.HasEPOAddress;

import static org.apache.commons.lang3.StringUtils.isEmpty;
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
