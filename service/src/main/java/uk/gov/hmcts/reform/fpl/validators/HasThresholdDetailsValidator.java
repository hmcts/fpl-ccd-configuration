package uk.gov.hmcts.reform.fpl.validators;

import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasThresholdDetails;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasThresholdDetailsValidator implements
    ConstraintValidator<HasThresholdDetails, CaseData> {

    @Override
    public void initialize(HasThresholdDetails constraintAnnotation) {
    }

    @Override
    public boolean isValid(CaseData caseData, ConstraintValidatorContext constraintValidatorContext) {
        constraintValidatorContext.buildConstraintViolationWithTemplate(constraintValidatorContext
            .getDefaultConstraintMessageTemplate()).addPropertyNode("groundsForTheApplication")
            .addConstraintViolation();

        if (caseData.hasEPOGrounds()) {

            return caseData.getGroundsForEPO() != null
                && StringUtils.isNotBlank(caseData.getGroundsForEPO().getThresholdDetails());
        } else {
            return true;
        }
    }
}
