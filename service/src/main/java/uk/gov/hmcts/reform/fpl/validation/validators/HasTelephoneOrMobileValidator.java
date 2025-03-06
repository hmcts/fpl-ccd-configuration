package uk.gov.hmcts.reform.fpl.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.fpl.model.interfaces.TelephoneContacts;
import uk.gov.hmcts.reform.fpl.validation.interfaces.HasTelephoneOrMobile;

public class HasTelephoneOrMobileValidator implements ConstraintValidator<HasTelephoneOrMobile, TelephoneContacts> {
    @Override
    public boolean isValid(TelephoneContacts telephoneContacts, ConstraintValidatorContext constraintValidatorContext) {
        return telephoneContacts.getTelephoneNumber() != null
            && StringUtils.isNotBlank(telephoneContacts.getTelephoneNumber().getTelephoneNumber())
            || telephoneContacts.getMobileNumber() != null
            && StringUtils.isNotBlank(telephoneContacts.getMobileNumber().getTelephoneNumber());
    }
}
