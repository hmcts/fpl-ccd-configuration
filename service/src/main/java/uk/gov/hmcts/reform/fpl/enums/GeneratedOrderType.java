package uk.gov.hmcts.reform.fpl.enums;

import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.EnumUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum GeneratedOrderType {
    @CCD(label = "Blank order (C21)")
    BLANK_ORDER("Blank order (C21)", "blank_order_c21.pdf"),
    @CCD(label = "Care order")
    CARE_ORDER("Care order", "care_order.pdf"),
    @CCD(label = "Emergency protection order")
    EMERGENCY_PROTECTION_ORDER("Emergency protection order", "emergency_protection_order.pdf"),
    @CCD(label = "Discharge of care order")
    DISCHARGE_OF_CARE_ORDER("Discharge of care order", "discharge_of_care_order.pdf"),
    @CCD(label = "Supervision order")
    SUPERVISION_ORDER("Supervision order", "supervision_order.pdf"),
    @CCD(label = "Upload a sealed order from family man")
    UPLOAD(null, null);

    private final String label;
    private final String fileName;

    public static GeneratedOrderType fromType(String type) {
        type = type.replaceAll("(Final|Interim|\\(C21\\))", "").strip();
        type = type.toUpperCase();
        type = type.replace(" ", "_");

        return Optional.ofNullable(
            EnumUtils.getEnum(GeneratedOrderType.class, type)
        ).orElse(UPLOAD);
    }

}
