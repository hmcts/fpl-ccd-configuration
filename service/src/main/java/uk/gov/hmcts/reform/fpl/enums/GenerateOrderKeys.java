package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

public enum GenerateOrderKeys {
    ORDER_TYPE_AND_DOCUMENT("orderTypeAndDocument"),
    ORDER("order"),
    JUDGE_AND_LEGAL_ADVISOR("judgeAndLegalAdvisor"),
    ORDER_FURTHER_DIRECTIONS("orderFurtherDirections");

    @Getter
    private final String key;

    GenerateOrderKeys(String key) {
        this.key = key;
    }
}
