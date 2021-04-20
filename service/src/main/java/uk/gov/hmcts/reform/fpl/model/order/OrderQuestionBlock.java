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
    APPROVAL_DATE_TIME("approvalDateTime", "Approval Date Time", OrderSection.ISSUING_DETAILS,
        List.of("manageOrdersApprovalDateTime")),
    WHICH_CHILDREN("whichChildren", "Which children", OrderSection.CHILDREN_DETAILS,
        List.of("orderAppliesToAllChildren", "children_label", "childSelector")),
    EPO_ORDER_DETAILS("epoOrderDetails", "EPO Order details", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersEpoType", "manageOrdersIncludePhrase", "manageOrdersChildrenDescription",
            "manageOrdersFurtherDirections", "manageOrdersEndDateTime")),
    EPO_REMOVAL_ADDRESS("epoRemovalAddress", "EPO Removal Address", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersEpoRemovalAddress")),
    EPO_EXCLUSION("epoExclusion", "EPO Exclusion", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersExclusionRequirement", "manageOrdersWhoIsExcluded", "manageOrdersExclusionStartDate",
            "manageOrdersPowerOfArrest")),
    FURTHER_DIRECTIONS("furtherDirections", "Further Directions", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersFurtherDirections")),
    REVIEW_DRAFT_ORDER("previewOrder", "Review draft order", OrderSection.REVIEW,
        List.of("orderPreview"));


    private final String showHideField;
    private final String question;
    private final OrderSection section;
    private final List<String> dataFields;
}
