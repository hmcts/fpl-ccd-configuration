package uk.gov.hmcts.reform.fpl.model.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum OrderQuestionBlock {
    LINKED_TO_HEARING("hearingDetails", "Linked to hearing", OrderSection.HEARING_DETAILS,
        List.of("manageOrdersApprovedAtHearing", "manageOrdersApprovedAtHearingList",
            "manageOrdersApprovalDate", "manageOrdersApprovalDateTime", "judgeAndLegalAdvisor")),
    APPROVER("approver", "Approver", OrderSection.ISSUING_DETAILS,
        List.of("judgeAndLegalAdvisor")),
    APPROVAL_DATE("approvalDate", "Approval Date", OrderSection.ISSUING_DETAILS,
        List.of("manageOrdersApprovalDate")),
    APPROVAL_DATE_TIME("approvalDateTime", "Approval Date Time", OrderSection.ISSUING_DETAILS,
        List.of("manageOrdersApprovalDateTime")),
    WHICH_CHILDREN("whichChildren", "Which children", OrderSection.CHILDREN_DETAILS,
        List.of("orderAppliesToAllChildren", "children_label", "childSelector")),
    EPO_INCLUDE_PHRASE("epoIncludePhrase", "Include Phrase", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersIncludePhrase")),
    EPO_CHILDREN_DESCRIPTION("epoChildrenDescription", "Children description", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersChildrenDescription")),
    EPO_EXPIRY_DATE("epoExpiryDate", "End date", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersEndDateTime")),
    EPO_TYPE_AND_PREVENT_REMOVAL("epoTypeAndPreventRemoval", "Prevent removal", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersEpoType", "manageOrdersEpoRemovalAddress", "manageOrdersExclusionRequirement",
            "manageOrdersWhoIsExcluded", "manageOrdersExclusionStartDate", "manageOrdersPowerOfArrest")),
    FURTHER_DIRECTIONS("furtherDirections", "Further Directions", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersFurtherDirections")),
    DETAILS("orderDetails", "Order Details", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersTitle", "manageOrdersDirections")),
    REVIEW_DRAFT_ORDER("previewOrder", "Review draft order", OrderSection.REVIEW,
        List.of("orderPreview")),
    SUPERVISION_ORDER_END_DATE("supervisionOrderExpiryDate", "End date", OrderSection.ORDER_DETAILS,
        List.of("manageSupervisionOrderEndDateType", "manageOrdersSetDateEndDate", "manageOrdersSetDateAndTimeEndDate",
            "manageOrdersSetMonthsEndDate"));

    private final String showHideField;
    private final String question;
    private final OrderSection section;
    private final List<String> dataFields;
}
