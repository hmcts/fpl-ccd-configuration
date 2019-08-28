package uk.gov.hmcts.reform.fpl.validators;

import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasChildName;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasChildNameValidator implements ConstraintValidator<HasChildName, ChildParty> {
    @Override
    public boolean isValid(ChildParty childParty, ConstraintValidatorContext constraintValidatorContext) {
        return StringUtils.isNotBlank(childParty.getFirstName()) || StringUtils.isNotBlank(childParty.getLastName());
    }
}
