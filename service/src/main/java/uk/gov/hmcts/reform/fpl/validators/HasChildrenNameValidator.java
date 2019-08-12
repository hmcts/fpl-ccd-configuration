package uk.gov.hmcts.reform.fpl.validators;

import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Children;
import uk.gov.hmcts.reform.fpl.validators.interfaces.HasChildName;

import java.util.Objects;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasChildrenNameValidator implements ConstraintValidator<HasChildName, Children> {

    @Override
    public void initialize(HasChildName constraintAnnotation) {
    }

    @Override
    public boolean isValid(Children children, ConstraintValidatorContext constraintValidatorContext) {

        if (children.getAdditionalChildren() != null || children.getFirstChild() != null) {
            return children.getAllChildren().stream()
                .filter(Objects::nonNull)
                .map(Child::getChildName)
                .allMatch(childName -> childName != null && !childName.isBlank());
        } else {
            return false;
        }
    }
}
