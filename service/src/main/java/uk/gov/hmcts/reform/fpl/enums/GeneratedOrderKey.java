package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GeneratedOrderKey {
    ORDER_TYPE_AND_DOCUMENT("orderTypeAndDocument"),
    ORDER("order"),
    JUDGE_AND_LEGAL_ADVISOR("judgeAndLegalAdvisor"),
    ORDER_FURTHER_DIRECTIONS("orderFurtherDirections"),
    ORDER_MONTHS("orderMonths"),
    DATE_OF_ISSUE("dateOfIssue"),
    PAGE_SHOW("pageShow"),
    CHILDREN_LABEL("children_label"),
    CHILD_SELECTOR("childSelector"),
    ORDER_APPLIES_TO_ALL_CHILDREN("orderAppliesToAllChildren"),
    CLOSE_CASE_LABEL("close_case_label"),
    CLOSE_CASE_FROM_ORDER("closeCaseFromOrder"),
    SHOW_CLOSE_CASE_PAGE("showCloseCaseFromOrderPage"),
    REMAINING_CHILD_INDEX("remainingChildIndex"),
    REMAINING_CHILD("remainingChild"),
    OTHER_FINAL_ORDER_CHILDREN("otherFinalOrderChildren"),
    SHOW_FINAL_ORDER_SINGLE_CHILD("showFinalOrderSingleChildPage"),
    SINGLE_CARE_ORDER_LABEL("singleCareOrder_label"),
    MULTIPLE_CARE_ORDER_LABEL("multipleCareOrder_label"),
    CARE_ORDER_SELECTOR("careOrderSelector"),
    NEW_HEARING_SELECTOR("newHearingSelector"),
    NEW_HEARING_LABEL("newHearing_label"),
    READ_ONLY_FAMILY_MAN_NUMBER("readOnlyFamilyManCaseNumber"),
    READ_ONLY_ORDER("readOnlyOrder"),
    READ_ONLY_CHILDREN("readOnlyChildren"),
    UPLOADED_ORDER("uploadedOrder");

    private final String key;
}
