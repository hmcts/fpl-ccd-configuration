package uk.gov.hmcts.reform.fpl.service.orders.validator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Component
public class WhichOrderValidator implements QuestionBlockOrderValidator {

    private static final String MESSAGE = "Select care orders to be discharged";

    @Override
    public OrderQuestionBlock accept() {
        return OrderQuestionBlock.WHICH_ORDERS;
    }

    @Override
    public List<String> validate(CaseData caseData) {
        Selector careOrderSelector = caseData.getCareOrderSelector();

        if (isEmpty(careOrderSelector) || isEmpty(careOrderSelector.getSelected())) {
            return List.of(MESSAGE);
        }

        return List.of();
    }
}
