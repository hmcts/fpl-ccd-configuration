package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

@Getter
public enum FinalOrderType {
    BLANK_ORDER("Blank order (C21)"),
    CARE_ORDER("Care order");

    private final String type;

    FinalOrderType(String type) {
        this.type = type;
    }
}
