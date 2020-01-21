package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GeneratedOrderType {
    BLANK_ORDER("Blank order (C21)", "blank_order_c21.pdf"),
    CARE_ORDER("Care order", "care_order.pdf"),
    EMERGENCY_PROTECTION_ORDER("Emergency protection order", "emergency_protection_order.pdf"),
    SUPERVISION_ORDER("Supervision order", "supervision_order.pdf");

    private final String label;
    private final String fileName;
}
