package uk.gov.hmcts.reform.fpl.validation.validators;

import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.validation.interfaces.HasEnteredExcluded;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static java.util.Objects.isNull;
import static org.xhtmlrenderer.util.Util.isNullOrEmpty;
import static uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType.EXCLUSION_REQUIREMENT;

public class HasEnteredExcludedValidator implements ConstraintValidator<HasEnteredExcluded, Orders> {
    @Override
    public boolean isValid(Orders value, ConstraintValidatorContext context) {
        if (orderDirectionsContainExclusion(value) && isNullOrEmpty(value.getExcluded())) {
            return false;
        }
        return true;
    }

    private boolean orderDirectionsContainExclusion(Orders orders) {
        if (!isNull(orders.getEmergencyProtectionOrderDirections()) && orders.getEmergencyProtectionOrderDirections()
            .contains(EXCLUSION_REQUIREMENT)) {
            return true;
        }
        return false;
    }
}
