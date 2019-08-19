package uk.gov.hmcts.reform.fpl.validators;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasMainApplicant;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasMainApplicantValidator implements ConstraintValidator<HasMainApplicant, CaseData> {
    @Override
    public void initialize(HasMainApplicant constraintAnnotation) {
    }

    @Override
    public boolean isValid(CaseData caseData, ConstraintValidatorContext constraintValidatorContext) {
        constraintValidatorContext.buildConstraintViolationWithTemplate(constraintValidatorContext
            .getDefaultConstraintMessageTemplate()).addPropertyNode("mainApplicant")
            .addConstraintViolation();

        return caseData.getMainApplicant() != null && caseData.getMainApplicant().getParty() != null;
    }
}
