package uk.gov.hmcts.reform.fpl.model.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPROVAL_DATE;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPROVAL_DATE_TIME;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPROVER;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.CAFCASS_JURISDICTIONS;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.CLOSE_CASE;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.DETAILS;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.EPO_CHILDREN_DESCRIPTION;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.EPO_EXPIRY_DATE;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.EPO_INCLUDE_PHRASE;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.EPO_TYPE_AND_PREVENT_REMOVAL;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.FURTHER_DIRECTIONS;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.ICO_EXCLUSION;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.LINKED_TO_HEARING;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.MANAGE_ORDER_END_DATE_WITH_END_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.MANAGE_ORDER_END_DATE_WITH_MONTH;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.REVIEW_DRAFT_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.WHICH_CHILDREN;

@Getter
@RequiredArgsConstructor
public enum Order {
    C21_BLANK_ORDER(
        "Blank order",
        "Section 31 Children Act 1989",
        "C21 - Blank order",
        false,
        List.of(LINKED_TO_HEARING, APPROVER, APPROVAL_DATE, WHICH_CHILDREN, DETAILS, REVIEW_DRAFT_ORDER)
    ),
    C23_EMERGENCY_PROTECTION_ORDER(
        "Emergency protection order",
        "Section 44 Children Act 1989",
        "C23 - Emergency protection order",
        false,
        List.of(LINKED_TO_HEARING, APPROVER, APPROVAL_DATE_TIME, WHICH_CHILDREN, EPO_TYPE_AND_PREVENT_REMOVAL,
            EPO_INCLUDE_PHRASE, EPO_CHILDREN_DESCRIPTION, EPO_EXPIRY_DATE, FURTHER_DIRECTIONS, REVIEW_DRAFT_ORDER)
    ),
    C32_CARE_ORDER(
        "Care order",
        "Section 31 Children Act 1989",
        "C32 - Care order",
        true,
        List.of(LINKED_TO_HEARING, APPROVER, APPROVAL_DATE, WHICH_CHILDREN, FURTHER_DIRECTIONS, REVIEW_DRAFT_ORDER,
            CLOSE_CASE)
    ),
    C35A_SUPERVISION_ORDER(
        "Supervision order",
        "Section 31 and Paragraphs 1 and 2 Schedule 3 Children Act 1989",
        "Supervision order (C35A)",
        true,
        List.of(
            LINKED_TO_HEARING, APPROVER, APPROVAL_DATE, WHICH_CHILDREN, FURTHER_DIRECTIONS, MANAGE_ORDER_END_DATE_WITH_MONTH,
            REVIEW_DRAFT_ORDER, CLOSE_CASE, REVIEW_DRAFT_ORDER)
    ),
    C33_INTERIM_CARE_ORDER(
        "Interim care order",
        "Section 38 Children Act 1989",
        "Interim care order (C33)",
        false,
        List.of(
            LINKED_TO_HEARING, APPROVER, APPROVAL_DATE, WHICH_CHILDREN, ICO_EXCLUSION, FURTHER_DIRECTIONS,
            MANAGE_ORDER_END_DATE_WITH_END_OF_PROCEEDINGS, REVIEW_DRAFT_ORDER)
    ),
    C47A_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN(
        "Appointment of a Children's Guardian",
        "Section 41(1) Children Act 1989",
        "C47A - Appointment of a Children's Guardian",
        false,
        List.of(
            LINKED_TO_HEARING, APPROVER, APPROVAL_DATE, CAFCASS_JURISDICTIONS, FURTHER_DIRECTIONS, REVIEW_DRAFT_ORDER)
    );

    private final String title;
    private final String childrenAct;
    private final String historyTitle;
    private final boolean isOrderFinal;
    private final List<OrderQuestionBlock> questions;

    public String fileName(RenderFormat format) {
        return String.format("%s.%s", this.name().toLowerCase(), format.getExtension());
    }

    public OrderSection firstSection() {
        return this.getQuestions().get(0).getSection();
    }

    public Optional<OrderSection> nextSection(OrderSection currentSection) {
        Set<OrderSection> sectionsForOrder = this.getQuestions()
            .stream()
            .map(OrderQuestionBlock::getSection)
            .collect(Collectors.toSet());

        for (int i = 0; i < OrderSection.values().length - 1; i++) {
            if (currentSection.equals(OrderSection.values()[i])) { // current section found
                for (int j = i + 1; j < OrderSection.values().length; j++) { // assume sections in order
                    if (sectionsForOrder.contains(OrderSection.values()[j])) { // question sections contain section
                        return Optional.of(OrderSection.values()[j]);
                    }
                }
            }
        }

        return Optional.empty();
    }
}
