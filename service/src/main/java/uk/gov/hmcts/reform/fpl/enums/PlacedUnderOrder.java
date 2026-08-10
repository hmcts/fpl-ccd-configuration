package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
public enum PlacedUnderOrder {
    @CCD(label = "Care order")
    CARE_ORDER("Care order"),
    @CCD(label = "Emergency protection order")
    EMERGENCY_PROTECTION_ORDER("Emergency protection order");

    private final String label;

    PlacedUnderOrder(String label) {
        this.label = label;
    }
}
