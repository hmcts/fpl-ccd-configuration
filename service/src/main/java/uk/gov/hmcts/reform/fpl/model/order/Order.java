package uk.gov.hmcts.reform.fpl.model.order;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.QUESTION_BLOCK_A;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.QUESTION_BLOCK_B;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.QUESTION_BLOCK_C;

public enum Order {
    ORDER_1(List.of(QUESTION_BLOCK_A)),
    ORDER_2(List.of(QUESTION_BLOCK_A, QUESTION_BLOCK_B)),
    ORDER_3(List.of(QUESTION_BLOCK_A, QUESTION_BLOCK_C)),
    ORDER_4(List.of(QUESTION_BLOCK_A, QUESTION_BLOCK_C));

    private final List<OrderQuestionBlock> questions;

    Order(List<OrderQuestionBlock> questions) {
        this.questions = questions;
    }

    public List<OrderQuestionBlock> getQuestions() {
        return questions;
    }
}
