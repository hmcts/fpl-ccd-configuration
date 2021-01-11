package uk.gov.hmcts.reform.fpl.validation.validators.epo;

import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.validation.interfaces.epo.HasEnteredEPOExcluded;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static java.util.Objects.isNull;
import static org.apache.logging.log4j.util.Strings.isEmpty;
import static uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType.EXCLUSION_REQUIREMENT;

public class HasEnteredEPOExcludedValidator implements ConstraintValidator<HasEnteredEPOExcluded, Orders> {
    private final FeatureToggleService featureToggleService;

    public HasEnteredEPOExcludedValidator(FeatureToggleService featureToggleService) {
        this.featureToggleService = featureToggleService;
    }

    @Override
    public boolean isValid(Orders value, ConstraintValidatorContext context) {
        if (featureToggleService.isEpoOrderTypeAndExclusionEnabled()) {
            return !value.orderContainsEPO() || !directionsContainExclusionRequirement(value)
                || !isEmpty(value.getExcluded());
        }
        return true;
    }

    private boolean directionsContainExclusionRequirement(Orders orders) {
        return !isNull(orders.getEmergencyProtectionOrderDirections()) && orders.getEmergencyProtectionOrderDirections()
            .contains(EXCLUSION_REQUIREMENT);
    }
}
