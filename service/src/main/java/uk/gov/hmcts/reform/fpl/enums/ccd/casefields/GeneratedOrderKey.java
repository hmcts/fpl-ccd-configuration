package uk.gov.hmcts.reform.fpl.enums.ccd.casefields;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum GeneratedOrderKey implements CaseField {
    ORDER_TYPE_AND_DOCUMENT("orderTypeAndDocument"),
    ORDER("order"),
    JUDGE_AND_LEGAL_ADVISOR("judgeAndLegalAdvisor"),
    ORDER_FURTHER_DIRECTIONS("orderFurtherDirections"),
    ORDER_MONTHS("orderMonths");

    private final String key;

    public static Stream<GeneratedOrderKey> asStream() {
        return Arrays.stream(GeneratedOrderKey.values());
    }
}
