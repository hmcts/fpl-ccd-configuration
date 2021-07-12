package uk.gov.hmcts.reform.fpl.enums;

public enum AmendableOrderType {
    URGENT_HEARING_ORDER("Urgent hearing order"),
    STANDARD_DIRECTION_ORDER("Standard direction order"),
    CASE_MANAGEMENT_ORDER("Case management order");

    private final String label;

    AmendableOrderType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
