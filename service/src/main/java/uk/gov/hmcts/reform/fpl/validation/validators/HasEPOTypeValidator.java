package uk.gov.hmcts.reform.fpl.validation.validators;

import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.validation.interfaces.HasEPOType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static java.util.Objects.isNull;

public class HasEPOTypeValidator implements ConstraintValidator<HasEPOType, Orders> {
    private final FeatureToggleService featureToggleService;

    public HasEPOTypeValidator(FeatureToggleService featureToggleService) {
        this.featureToggleService = featureToggleService;
    }

    @Override
    public boolean isValid(Orders value, ConstraintValidatorContext context) {
        if (featureToggleService.isEpoOrderTypeAndExclusionEnabled()) {
            if (value.orderContainsEPO() && isNull(value.getEpoType())) {
                return false;
            }
            return true;
        }
        return true;
    }
}
