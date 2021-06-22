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
    LINK_APPLICATION("linkApplication", "Link application", OrderSection.HEARING_DETAILS,
        List.of("manageOrdersShouldLinkApplication", "manageOrdersLinkedApplication")),
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
    CAFCASS_JURISDICTIONS("cafcassJurisdictions", "Select jurisdiction", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersCafcassRegion", "manageOrdersCafcassOfficesEngland", "manageOrdersCafcassOfficesWales")),
    DETAILS("orderDetails", "Order Details", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersTitle", "manageOrdersDirections")),
    DISCHARGE_DETAILS("dischargeOfCareDetails", "Order Details", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersCareOrderIssuedDate", "manageOrdersCareOrderIssuedCourt")),
    REVIEW_DRAFT_ORDER("previewOrder", "Review draft order", OrderSection.REVIEW,
        List.of("orderPreview")),
    MANAGE_ORDER_END_DATE_WITH_MONTH(
        "manageOrdersExpiryDateWithMonth", "End date", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersEndDateTypeWithMonth", "manageOrdersSetDateEndDate", "manageOrdersSetDateAndTimeEndDate",
            "manageOrdersSetMonthsEndDate")),
    MANAGE_ORDER_END_DATE_WITH_END_OF_PROCEEDINGS(
        "manageOrdersExpiryDateWithEndOfProceedings", "End date", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersEndDateTypeWithEndOfProceedings", "manageOrdersSetDateEndDate",
            "manageOrdersSetDateAndTimeEndDate")),
    ICO_EXCLUSION(
        "manageOrdersExclusionRequirementDetails", "Add exclusion details", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersHasExclusionRequirement", "manageOrdersExclusionDetails")),
    CLOSE_CASE("closeCase", "Closing case", OrderSection.REVIEW,
        List.of("manageOrdersCloseCase", "manageOrdersCloseCaseWarning"));

    private final String showHideField;
    private final String question;
    private final OrderSection section;
    private final List<String> dataFields;
}
