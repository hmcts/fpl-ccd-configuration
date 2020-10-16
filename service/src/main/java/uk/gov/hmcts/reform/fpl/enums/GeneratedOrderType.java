package uk.gov.hmcts.reform.fpl.enums;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GeneratedOrderType {
    BLANK_ORDER("Blank order (C21)", "blank_order_c21.pdf"),
    CARE_ORDER("Care order", "care_order.pdf"),
    EMERGENCY_PROTECTION_ORDER("Emergency protection order", "emergency_protection_order.pdf"),
    DISCHARGE_OF_CARE_ORDER("Discharge of care order", "discharge_of_care_order.pdf"),
    SUPERVISION_ORDER("Supervision order", "supervision_order.pdf"),
    UPLOAD(null, null);

    private final String label;
    private final String fileName;

    public static GeneratedOrderType fromType(String type) {
        type = type.replaceAll("(Final|Interim|\\(C21\\))", "").strip();
        type = type.toUpperCase();
        type = type.replace(" ", "_");
        try {
            return GeneratedOrderType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return UPLOAD;
        }
    }

    @JsonIgnore
    public boolean isRemovable() {
        return this == BLANK_ORDER;
    }
}
