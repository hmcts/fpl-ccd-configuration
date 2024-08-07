package uk.gov.hmcts.reform.fpl.validation.validators.epo;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.validation.interfaces.epo.HasEPOType;

import static java.util.Objects.isNull;

public class HasEPOTypeValidator implements ConstraintValidator<HasEPOType, Orders> {

    @Override
    public boolean isValid(Orders value, ConstraintValidatorContext context) {
        return !value.orderContainsEPO() || !isNull(value.getEpoType());
    }
}
