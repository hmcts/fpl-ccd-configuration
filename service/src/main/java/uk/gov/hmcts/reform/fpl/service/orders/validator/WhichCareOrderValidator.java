package uk.gov.hmcts.reform.fpl.service.orders.validator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Component
public class WhichCareOrderValidator implements QuestionBlockOrderValidator {

    private static final String SELECT_ORDER_MESSAGE = "Select a care order to be discharged";
    private static final String SELECT_ONE_ORDER_MESSAGE = "Select one care order to discharge";

    @Override
    public OrderQuestionBlock accept() {
        return OrderQuestionBlock.WHICH_ORDERS;
    }

    @Override
    public List<String> validate(CaseData caseData) {
        Selector careOrderSelector = caseData.getCareOrderSelector();

        if (isEmpty(careOrderSelector) || isEmpty(careOrderSelector.getSelected())) {
            return List.of(SELECT_ORDER_MESSAGE);
        }

        if (careOrderSelector.getSelected().size() > 1) {
            return List.of(SELECT_ONE_ORDER_MESSAGE);
        }

        return List.of();
    }
}
