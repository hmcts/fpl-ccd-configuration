package uk.gov.hmcts.reform.fpl.service.orders;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.QUESTION_BLOCK_A;

public class QuestionBlock1OrderValidator implements QuestionBlockOrderValidator {

    @Override
    public OrderQuestionBlock accept() {
        return QUESTION_BLOCK_A;
    }

    @Override
    public List<String> validate(CaseDetails caseData) {
        String question1 = (String) caseData.getData().get("manageOrderQuestion1");
        if (!"MAGICWORD".equals(question1)) {
            return List.of("typed some other stuff: You need to type MAGICWORD");
        }
        return List.of();
    }
}
