package uk.gov.hmcts.reform.fpl.enums;

public enum OrderStatus {
    SEALED("Sealed"),
    DRAFT("Draft");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
