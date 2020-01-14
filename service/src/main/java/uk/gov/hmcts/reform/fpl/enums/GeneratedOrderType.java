package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

@Getter
public enum GeneratedOrderType {
    BLANK_ORDER("Blank order (C21)"),
    CARE_ORDER("Care order");

    private final String label;

    GeneratedOrderType(String label) {
        this.label = label;
    }

    public String getFullType() {
        return getFullType(null);
    }

    public String getFullType(GeneratedOrderSubtype subtype) {
        if (this != BLANK_ORDER) {
            return subtype.getLabel() + " " + this.getLabel().toLowerCase();
        } else {
            return this.getLabel();
        }
    }
}
