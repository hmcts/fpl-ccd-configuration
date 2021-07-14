package uk.gov.hmcts.reform.fpl.enums;

public enum AmendedOrderType {
    URGENT_HEARING_ORDER("urgent hearing order"),
    STANDARD_DIRECTION_ORDER("standard direction order"),
    CASE_MANAGEMENT_ORDER("case management order");

    private final String label;

    AmendedOrderType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
