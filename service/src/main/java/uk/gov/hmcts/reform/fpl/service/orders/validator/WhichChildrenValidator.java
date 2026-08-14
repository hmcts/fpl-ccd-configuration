package uk.gov.hmcts.reform.fpl.service.orders.validator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;

@Component
public class WhichChildrenValidator implements QuestionBlockOrderValidator {

    private static final String MESSAGE = "Select the children included in the order";

    @Override
    public OrderQuestionBlock accept() {
        return OrderQuestionBlock.WHICH_CHILDREN;
    }

    @Override
    public List<String> validate(CaseData caseData) {
        String orderAppliesToAllChildren = caseData.getOrderAppliesToAllChildren();
        Selector childSelector = caseData.getChildSelector();

        if (NO.getValue().equals(orderAppliesToAllChildren) && childSelector.getSelected().isEmpty()) {
            return List.of(MESSAGE);
        }

        return List.of();
    }
}
