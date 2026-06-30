package uk.gov.hmcts.reform.fpl.service.orders.validator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;

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
        DynamicMultiSelectList childSelectorForManageOrders = caseData.getChildSelectorForManageOrders();

        if (NO.getValue().equals(orderAppliesToAllChildren) && (childSelectorForManageOrders.getValue() == null
            || childSelectorForManageOrders.getValue().isEmpty())) {
            return List.of(MESSAGE);
        }

        return List.of();
    }
}
