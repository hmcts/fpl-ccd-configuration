package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {
    SEALED("Sealed"),
    DRAFT("Draft");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }
}
