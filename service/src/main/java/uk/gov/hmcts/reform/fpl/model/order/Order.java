package uk.gov.hmcts.reform.fpl.model.order;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPROVAL_DATE;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPROVER;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.FURTHER_DIRECTIONS;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.WHICH_CHILDREN;

public enum Order {
    C32_CARE_ORDER(List.of(APPROVER, APPROVAL_DATE, WHICH_CHILDREN, FURTHER_DIRECTIONS));

    private final List<OrderQuestionBlock> questions;

    Order(List<OrderQuestionBlock> questions) {
        this.questions = questions;
    }

    public List<OrderQuestionBlock> getQuestions() {
        return questions;
    }
}
