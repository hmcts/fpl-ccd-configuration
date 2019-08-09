package uk.gov.hmcts.reform.fpl.validators;

import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasTelephone;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasTelephoneValidator implements ConstraintValidator<HasTelephone, Applicant> {
    @Override
    public void initialize(HasTelephone constraintAnnotation) { }

    @Override
    public boolean isValid(Applicant applicant, ConstraintValidatorContext constraintValidatorContext) {
        if (applicant.getTelephone() == null && applicant.getMobile() == null) {
            return false;
        } else {
            return true;
        }
    }
}
