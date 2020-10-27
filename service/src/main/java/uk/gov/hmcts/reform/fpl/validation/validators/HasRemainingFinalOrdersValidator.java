package uk.gov.hmcts.reform.fpl.validation.validators;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.validation.interfaces.HasRemainingFinalOrders;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasRemainingFinalOrdersValidator implements ConstraintValidator<HasRemainingFinalOrders, CaseData> {
    @Override
    public boolean isValid(CaseData caseData, ConstraintValidatorContext constraintValidatorContext) {
        if (caseData.getChildren1() == null) {
            return true;
        }

        return caseData.getChildren1().stream()
            .map(Element::getValue)
            .allMatch(this::hasFinalFields);
    }

    private boolean hasFinalFields(Child child) {
        return child.getFinalOrderIssuedType() != null && child.getFinalOrderIssued() != null;
    }
}
