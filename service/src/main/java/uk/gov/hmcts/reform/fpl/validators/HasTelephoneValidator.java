package uk.gov.hmcts.reform.fpl.validators;

import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasTelephone;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasTelephoneValidator implements ConstraintValidator<HasTelephone, Applicant> {
    @Override
    public void initialize(HasTelephone constraintAnnotation) {
    }

    @Override
    public boolean isValid(Applicant applicant, ConstraintValidatorContext constraintValidatorContext) {
        return StringUtils.isNotBlank(applicant.getTelephone()) || StringUtils.isNotBlank(applicant.getMobile());
    }
}
