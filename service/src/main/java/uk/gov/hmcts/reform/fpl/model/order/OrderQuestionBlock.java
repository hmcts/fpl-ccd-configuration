package uk.gov.hmcts.reform.fpl.model.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum OrderQuestionBlock {
    APPROVER("approver", "Approver", OrderSection.SECTION_2,
        List.of("judgeAndLegalAdvisor")),
    APPROVAL_DATE("approvalDate", "Approval Date", OrderSection.SECTION_2,
        List.of("manageOrdersApprovalDate")),
    WHICH_CHILDREN("whichChildren", "Which children", OrderSection.SECTION_3,
        List.of("orderAppliesToAllChildren", "children_label", "childSelector")),
    FURTHER_DIRECTIONS("furtherDirections", "Further Directions", OrderSection.SECTION_4,
        List.of("manageOrdersFurtherDirections")),
    REVIEW_DRAFT_ORDER("draftOrder", "Review draft order",OrderSection.REVIEW,
        List.of("something"));


    private final String showHideField;
    private final String question;
    private final OrderSection section;
    private final List<String> dataFields;
}
