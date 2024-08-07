package uk.gov.hmcts.reform.fpl.validation.validators.epo;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.validation.interfaces.epo.HasEnteredEPOExcluded;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType.EXCLUSION_REQUIREMENT;

public class HasEnteredEPOExcludedValidator implements ConstraintValidator<HasEnteredEPOExcluded, Orders> {
    @Override
    public boolean isValid(Orders value, ConstraintValidatorContext context) {
        return !value.orderContainsEPO() || !directionsContainExclusionRequirement(value)
            || !isEmpty(value.getExcluded());
    }

    private boolean directionsContainExclusionRequirement(Orders orders) {
        return !isNull(orders.getEmergencyProtectionOrderDirections()) && orders.getEmergencyProtectionOrderDirections()
            .contains(EXCLUSION_REQUIREMENT);
    }
}
