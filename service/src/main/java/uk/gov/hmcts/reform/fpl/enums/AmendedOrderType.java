package uk.gov.hmcts.reform.fpl.enums;

public enum AmendedOrderType {
    URGENT_HEARING_ORDER("Urgent hearing order"),
    STANDARD_DIRECTION_ORDER("Standard direction order"),
    CASE_MANAGEMENT_ORDER("Case management order");

    private final String label;

    AmendedOrderType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
