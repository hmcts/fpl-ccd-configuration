package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

@Getter
public enum GeneratedOrderType {
    BLANK_ORDER("Blank order (C21)"),
    CARE_ORDER("Care order");

    private final String type;

    GeneratedOrderType(String type) {
        this.type = type;
    }
}
