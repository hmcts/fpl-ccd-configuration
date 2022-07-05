package uk.gov.hmcts.reform.fpl.service.orders.validator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.List;

@Component
public class RespondentsRefusedValidator implements QuestionBlockOrderValidator {

    private static final String MESSAGE = "Select the appointed person(s) being refused contact";

    @Override
    public OrderQuestionBlock accept() {
        return OrderQuestionBlock.RESPONDENTS_REFUSED;
    }

    @Override
    public List<String> validate(CaseData caseData) {
        Selector respondentsRefusedSelector = caseData.getRespondentsRefusedSelector();

        if (respondentsRefusedSelector.getSelected().isEmpty()) {
            return List.of(MESSAGE);
        }

        return List.of();
    }
}
