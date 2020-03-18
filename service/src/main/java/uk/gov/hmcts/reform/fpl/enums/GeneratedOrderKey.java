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
    ORDER_APPLIES_TO_ALL_CHILDREN("orderAppliesToAllChildren");

    private final String key;
}
