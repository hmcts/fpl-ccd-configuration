package uk.gov.hmcts.reform.fpl.validators;

import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.fpl.model.interfaces.TelephoneContacts;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasContactDirection;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasContactDirectionValidator implements ConstraintValidator<HasContactDirection, TelephoneContacts> {
    @Override
    public boolean isValid(TelephoneContacts telephoneContacts, ConstraintValidatorContext constraintValidatorContext) {
        return telephoneContacts.getTelephoneNumber() != null
            && StringUtils.isNotBlank(telephoneContacts.getTelephoneNumber().getContactDirection());
    }
}
