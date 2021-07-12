package uk.gov.hmcts.reform.fpl.model.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum OrderQuestionBlock {

    LINKED_TO_HEARING("hearingDetails", OrderSection.HEARING_DETAILS,
        List.of("manageOrdersApprovedAtHearing", "manageOrdersApprovedAtHearingList",
            "manageOrdersApprovalDate", "manageOrdersApprovalDateTime", "judgeAndLegalAdvisor")),
    LINK_APPLICATION("linkApplication", OrderSection.HEARING_DETAILS,
        List.of("manageOrdersShouldLinkApplication", "manageOrdersLinkedApplication")),
    APPROVER("approver", OrderSection.ISSUING_DETAILS,
        List.of("judgeAndLegalAdvisor")),
    APPROVAL_DATE("approvalDate", OrderSection.ISSUING_DETAILS,
        List.of("manageOrdersApprovalDate")),
    APPROVAL_DATE_TIME("approvalDateTime", OrderSection.ISSUING_DETAILS,
        List.of("manageOrdersApprovalDateTime")),
    WHICH_CHILDREN("whichChildren", OrderSection.CHILDREN_DETAILS,
        List.of("orderAppliesToAllChildren", "children_label", "childSelector")),
    SELECT_SINGLE_CHILD("selectSingleChild", OrderSection.CHILDREN_DETAILS, List.of("whichChildIsTheOrderFor")),
    EPO_INCLUDE_PHRASE("epoIncludePhrase", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersIncludePhrase")),
    EPO_CHILDREN_DESCRIPTION("epoChildrenDescription", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersChildrenDescription")),
    EPO_EXPIRY_DATE("epoExpiryDate", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersEndDateTime")),
    EPO_TYPE_AND_PREVENT_REMOVAL("epoTypeAndPreventRemoval", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersEpoType", "manageOrdersEpoRemovalAddress", "manageOrdersExclusionRequirement",
            "manageOrdersWhoIsExcluded", "manageOrdersExclusionStartDate", "manageOrdersPowerOfArrest")),
    TITLE("orderTitle", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersTitle")),
    FURTHER_DIRECTIONS("furtherDirections", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersFurtherDirections")),
    REASON_FOR_SECURE_ACCOMMODATION("reasonForSecureAccommodation", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersReasonForSecureAccommodation")),
    IS_CHILD_REPRESENTED("childLegalRepresentation", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersIsChildRepresented")),
    SECURE_ACCOMMODATION_ORDER_JURISDICTION("orderJurisdiction",
        OrderSection.ORDER_DETAILS, List.of("manageOrdersOrderJurisdiction")),
    CAFCASS_JURISDICTIONS("cafcassJurisdictions", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersCafcassRegion", "manageOrdersCafcassOfficesEngland", "manageOrdersCafcassOfficesWales")),
    DETAILS("orderDetails", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersDirections")),
    CHILD_ARRANGEMENT_SPECIFIC_ISSUE_PROHIBITED_STEPS("childArrangementSpecificIssueProhibitedSteps",
        OrderSection.ORDER_DETAILS,
        List.of("manageOrdersMultiSelectListForC43", "manageOrdersRecitalsAndPreambles")),
    DISCHARGE_DETAILS("dischargeOfCareDetails", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersCareOrderIssuedDate", "manageOrdersCareOrderIssuedCourt")),
    REVIEW_DRAFT_ORDER("previewOrder", OrderSection.REVIEW,
        List.of("orderPreview")),
    APPOINTED_GUARDIAN("appointedGuardian", OrderSection.ORDER_DETAILS,
        List.of("appointedGuardians_label", "appointedGuardianSelector")),
    ORDER_BY_CONSENT("orderIsByConsent", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersIsByConsent")),
    MANAGE_ORDER_END_DATE_WITH_MONTH(
        "manageOrdersExpiryDateWithMonth", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersEndDateTypeWithMonth", "manageOrdersSetDateEndDate", "manageOrdersSetDateAndTimeEndDate",
            "manageOrdersSetMonthsEndDate")),
    MANAGE_ORDER_END_DATE_WITH_END_OF_PROCEEDINGS(
        "manageOrdersExpiryDateWithEndOfProceedings", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersEndDateTypeWithEndOfProceedings", "manageOrdersSetDateEndDate",
            "manageOrdersSetDateAndTimeEndDate")),
    NEED_SEALING("needSealing", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersNeedSealing")),
    UPLOAD_ORDER_FILE("uploadOrderFile", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersUploadOrderFile")),
    ICO_EXCLUSION(
        "manageOrdersExclusionRequirementDetails", OrderSection.ORDER_DETAILS,
        List.of("manageOrdersHasExclusionRequirement", "manageOrdersExclusionDetails")),
    CLOSE_CASE("closeCase", OrderSection.REVIEW,
        List.of("manageOrdersCloseCase", "manageOrdersCloseCaseWarning")),
    WHICH_OTHERS("whichOthers", OrderSection.OTHER_DETAILS,
        List.of("sendOrderToAllOthers", "others_label", "othersSelector"));

    private final String showHideField;
    private final OrderSection section;
    private final List<String> transientDataFields;
}
