package uk.gov.hmcts.reform.fpl.validation.validators;

import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;
import uk.gov.hmcts.reform.fpl.validation.interfaces.IsValidEmailAddress;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IsValidEmailAddressValidator implements ConstraintValidator<IsValidEmailAddress, String> {
    private final ValidateEmailService validateEmailService;

    public IsValidEmailAddressValidator(ValidateEmailService validateEmailService) {
        this.validateEmailService = validateEmailService;
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        return Boolean.valueOf(validateEmailService.validate(email));
    }
}
