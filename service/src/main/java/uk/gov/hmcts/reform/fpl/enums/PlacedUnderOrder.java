package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

@Getter
public enum PlacedUnderOrder {
    CARE_ORDER("Care order"),
    EMERGENCY_PROTECTION_ORDER("Emergency protection order");

    private final String label;

    PlacedUnderOrder(String label) {
        this.label = label;
    }
}
