package uk.gov.hmcts.reform.fpl.validators;

import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasTelephoneOrMobile;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasTelephoneOrMobileValidator implements ConstraintValidator<HasTelephoneOrMobile, ApplicantParty> {
    @Override
    public boolean isValid(ApplicantParty applicantParty, ConstraintValidatorContext constraintValidatorContext) {
        return applicantParty.getTelephoneNumber() != null
            && StringUtils.isNotBlank(applicantParty.getTelephoneNumber().getTelephoneNumber())
            || applicantParty.getMobileNumber() != null
            && StringUtils.isNotBlank(applicantParty.getMobileNumber().getTelephoneNumber());
    }
}
