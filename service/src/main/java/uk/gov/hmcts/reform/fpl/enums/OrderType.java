package uk.gov.hmcts.reform.fpl.enums;

public enum OrderType {
    CARE_ORDER("Care order"),
    INTERIM_CARE_ORDER("Interim care order"),
    SUPERVISION_ORDER("Supervision order"),
    INTERIM_SUPERVISION_ORDER("Interim supervision order"),
    EDUCATION_SUPERVISION_ORDER("Education supervision order"),
    EMERGENCY_PROTECTION_ORDER("Emergency protection order"),
    OTHER("Variation or discharge of care or supervision order");

    private final String label;

    OrderType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
