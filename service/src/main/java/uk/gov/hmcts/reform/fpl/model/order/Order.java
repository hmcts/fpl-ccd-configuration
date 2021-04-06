package uk.gov.hmcts.reform.fpl.model.order;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPROVAL_DATE;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPROVER;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.FURTHER_DIRECTIONS;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.REVIEW_DRAFT_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.WHICH_CHILDREN;

public enum Order {
    C32_CARE_ORDER(List.of(APPROVER, APPROVAL_DATE, WHICH_CHILDREN, FURTHER_DIRECTIONS, REVIEW_DRAFT_ORDER));

    private final List<OrderQuestionBlock> questions;

    Order(List<OrderQuestionBlock> questions) {
        this.questions = questions;
    }

    public List<OrderQuestionBlock> getQuestions() {
        return questions;
    }

    public Optional<OrderSection> nextSection(OrderSection currentSection) {
        Set<OrderSection> sectionsForOrder = this.getQuestions()
            .stream()
            .map(OrderQuestionBlock::getSection)
            .collect(Collectors.toSet());

        for (int i = 0; i < OrderSection.values().length - 1; i++) {
            if (currentSection.equals(OrderSection.values()[i])) {
                for (int j = i + 1; j < OrderSection.values().length; j++) {
                    if (sectionsForOrder.contains(OrderSection.values()[j])) {
                        return Optional.of(OrderSection.values()[j]);
                    }
                }
            }
        }

        return Optional.empty();
    }
}
