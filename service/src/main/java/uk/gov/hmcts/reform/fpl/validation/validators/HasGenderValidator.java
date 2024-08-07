package uk.gov.hmcts.reform.fpl.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.fpl.enums.ChildGender;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.validation.interfaces.HasGender;

import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.enums.ChildGender.OTHER;

public class HasGenderValidator implements ConstraintValidator<HasGender, ChildParty> {
    @Override
    public boolean isValid(ChildParty child, ConstraintValidatorContext constraintValidatorContext) {
        String gender = OTHER == child.getGender() ? child.getGenderIdentification()
            : Optional.ofNullable(child.getGender()).map(ChildGender::getLabel).orElse(null);
        return StringUtils.isNotBlank(gender);
    }
}
