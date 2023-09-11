package uk.gov.hmcts.reform.fpl.validation.validators;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.validation.interfaces.HasGender;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.reform.fpl.enums.ChildGender.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.ChildGender.fromLabel;

public class HasGenderValidator implements ConstraintValidator<HasGender, ChildParty> {
    @Override
    public boolean isValid(ChildParty child, ConstraintValidatorContext constraintValidatorContext) {
        String gender = OTHER == child.getGender() ? child.getGenderIdentification() : child.getGender().getLabel();
        return StringUtils.isNotBlank(gender);
    }
}
