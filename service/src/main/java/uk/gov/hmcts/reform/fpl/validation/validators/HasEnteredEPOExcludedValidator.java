package uk.gov.hmcts.reform.fpl.validation.validators;

import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.validation.interfaces.HasEnteredEPOExcluded;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static java.util.Objects.isNull;
import static org.apache.logging.log4j.util.Strings.isEmpty;
import static uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType.EXCLUSION_REQUIREMENT;

public class HasEnteredEPOExcludedValidator implements ConstraintValidator<HasEnteredEPOExcluded, Orders> {
    @Override
    public boolean isValid(Orders value, ConstraintValidatorContext context) {
        if (value.orderContainsEPO() && directionsContainExclusionRequirement(value)
            && isEmpty(value.getExcluded())) {
            return false;
        }
        return true;
    }

    private boolean directionsContainExclusionRequirement(Orders orders) {
        return !isNull(orders.getEmergencyProtectionOrderDirections()) && orders.getEmergencyProtectionOrderDirections()
            .contains(EXCLUSION_REQUIREMENT);
    }
}
