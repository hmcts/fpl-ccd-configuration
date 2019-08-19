package uk.gov.hmcts.reform.fpl.validators;

import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasApplicantContactName;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasApplicantContactNameValidator implements ConstraintValidator<HasApplicantContactName, ApplicantParty> {
    @Override
    public void initialize(HasApplicantContactName constraintAnnotation) {
    }

    @Override
    public boolean isValid(ApplicantParty applicantParty, ConstraintValidatorContext constraintValidatorContext) {
        return applicantParty.getTelephoneNumber() != null
            && applicantParty.getTelephoneNumber().getContactDirection() != null
            && StringUtils.isNotBlank(applicantParty.getTelephoneNumber().getContactDirection());
    }
}
