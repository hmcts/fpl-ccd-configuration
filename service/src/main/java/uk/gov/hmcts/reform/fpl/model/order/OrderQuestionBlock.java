package uk.gov.hmcts.reform.fpl.model.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum OrderQuestionBlock {
    APPROVER("approver", "Approver", OrderSection.ISSUING_DETAILS,
        List.of("judgeAndLegalAdvisor")),
    APPROVAL_DATE("approvalDate", "Approval Date", OrderSection.ISSUING_DETAILS,
        List.of("manageOrdersApprovalDate")),
    WHICH_CHILDREN("whichChildren", "Which children", OrderSection.CHILDREN_DETAILS,
        List.of("orderAppliesToAllChildren", "children_label", "childSelector")),
    FURTHER_DIRECTIONS("furtherDirections", "Further Directions", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersFurtherDirections")),
    DETAILS("orderDetails", "Order Details", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersTitle", "manageOrdersDirections")),
    REVIEW_DRAFT_ORDER("previewOrder", "Review draft order",OrderSection.REVIEW,
        List.of("orderPreview"));


    private final String showHideField;
    private final String question;
    private final OrderSection section;
    private final List<String> dataFields;
}
