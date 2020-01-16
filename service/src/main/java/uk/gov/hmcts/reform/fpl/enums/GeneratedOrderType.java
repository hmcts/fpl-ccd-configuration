package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GeneratedOrderType {
    BLANK_ORDER("Blank order (C21)"),
    CARE_ORDER("Care order"),
    SUPERVISION_ORDER("Supervision order");

    private final String label;

    public String getFullType() {
        return getFullType(null);
    }

    public String getFullType(GeneratedOrderSubtype subtype) {
        if (subtype != null) {
            return subtype.getLabel() + " " + this.getLabel().toLowerCase();
        }
        return this.getLabel();

    }
}
