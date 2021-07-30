package uk.gov.hmcts.reform.fpl.enums;

public enum RemovalReason {
    DUPLICATE("Duplicate"),
    WRONG_CASE("Wrong case");

    private final String label;

    RemovalReason(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
