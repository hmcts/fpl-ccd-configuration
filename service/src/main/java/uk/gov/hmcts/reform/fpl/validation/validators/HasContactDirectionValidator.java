package uk.gov.hmcts.reform.fpl.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.fpl.model.interfaces.TelephoneContacts;
import uk.gov.hmcts.reform.fpl.validation.interfaces.HasContactDirection;

public class HasContactDirectionValidator implements ConstraintValidator<HasContactDirection, TelephoneContacts> {
    @Override
    public boolean isValid(TelephoneContacts telephoneContacts, ConstraintValidatorContext constraintValidatorContext) {
        return telephoneContacts.getTelephoneNumber() != null
            && StringUtils.isNotBlank(telephoneContacts.getTelephoneNumber().getContactDirection());
    }
}
