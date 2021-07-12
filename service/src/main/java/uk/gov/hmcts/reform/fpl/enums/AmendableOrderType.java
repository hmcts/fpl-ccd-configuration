package uk.gov.hmcts.reform.fpl.enums;

public enum AmendableOrderType {
    BLANK_ORDER("Blank order"),
    CARE_ORDER("Care order"),
    INTERIM_CARE_ORDER("Interim care order"),
    SUPERVISION_ORDER("Supervision order"),
    INTERIM_SUPERVISION_ORDER("Interim supervision order"),
    EDUCATION_SUPERVISION_ORDER("Education supervision order"),
    EMERGENCY_PROTECTION_ORDER("Emergency protection order"),
    OTHER("Variation or discharge of care or supervision order"),
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
