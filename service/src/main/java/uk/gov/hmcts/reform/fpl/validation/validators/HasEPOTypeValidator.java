package uk.gov.hmcts.reform.fpl.validation.validators;

import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.validation.interfaces.HasEPOType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static java.util.Objects.isNull;

public class HasEPOTypeValidator implements ConstraintValidator<HasEPOType, Orders> {
    @Override
    public boolean isValid(Orders value, ConstraintValidatorContext context) {
        if (value.orderContainsEPO() && isNull(value.getEpoType())) {
            return false;
        }
        return true;
    }
}
