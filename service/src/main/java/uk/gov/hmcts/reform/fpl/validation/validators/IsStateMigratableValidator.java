package uk.gov.hmcts.reform.fpl.validation.validators;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.validation.interfaces.IsStateMigratable;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.hmcts.reform.fpl.enums.State.CLOSED;

public class IsStateMigratableValidator implements ConstraintValidator<IsStateMigratable, CaseData> {
    @Override
    public boolean isValid(CaseData caseData, ConstraintValidatorContext constraintValidatorContext) {
        if (CLOSED.equals(caseData.getState()) && caseData.getChildren1() != null) {
            return caseData.getChildren1().stream()
                .map(Element::getValue)
                .noneMatch(this::hasFinalFields);
        }

        return true;
    }

    private boolean hasFinalFields(Child child) {
        return child.getFinalOrderIssuedType() != null || child.getFinalOrderIssued() != null;
    }
}
