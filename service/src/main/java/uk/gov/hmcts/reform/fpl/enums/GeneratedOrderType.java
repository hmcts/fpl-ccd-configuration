package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

@Getter
public enum GeneratedOrderType {
    BLANK_ORDER("Blank order (C21)"),
    CARE_ORDER("Care order"),
    EMERGENCY_PROTECTION_ORDER("Emergency protection order");

    private final String label;

    GeneratedOrderType(String label) {
        this.label = label;
    }
}
