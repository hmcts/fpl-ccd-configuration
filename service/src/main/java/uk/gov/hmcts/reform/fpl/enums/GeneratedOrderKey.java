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
    PAGE_SHOW("pageShow"),
    CHILDREN_LABEL("children_label"),
    CHILD_SELECTOR("childSelector"),
    ALL_CHILDREN_CHOICE("allChildrenChoice");

    private final String key;
}
