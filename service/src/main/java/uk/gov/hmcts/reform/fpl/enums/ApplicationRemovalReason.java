package uk.gov.hmcts.reform.fpl.enums;

public enum ApplicationRemovalReason {
    DUPLICATE("Duplicate"),
    WRONG_CASE("Wrong case"),
    OTHER("Other");

    private final String label;

    ApplicationRemovalReason(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
