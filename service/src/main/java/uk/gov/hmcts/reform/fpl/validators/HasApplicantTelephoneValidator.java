package uk.gov.hmcts.reform.fpl.validators;

import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasApplicantTelephone;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasApplicantTelephoneValidator implements ConstraintValidator<HasApplicantTelephone, ApplicantParty> {
    @Override
    public boolean isValid(ApplicantParty applicantParty, ConstraintValidatorContext constraintValidatorContext) {
        return applicantParty.getTelephoneNumber() != null
            && StringUtils.isNotBlank(applicantParty.getTelephoneNumber().getTelephoneNumber())
            || applicantParty.getMobileNumber() != null
            && StringUtils.isNotBlank(applicantParty.getMobileNumber().getTelephoneNumber());
    }
}
