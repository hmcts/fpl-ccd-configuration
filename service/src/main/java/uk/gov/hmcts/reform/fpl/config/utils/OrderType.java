package uk.gov.hmcts.reform.fpl.config.utils;

public enum OrderType {

    CARE_ORDER("Care order"),
    INTERIM_CARE_ORDER("Interim care order"),
    SUPERVISION_ORDER("Supervision order"),
    INTERIM_SUPERVISION_ORDER("Interim supervision order"),
    EDUCATION_SUPERVISION_ORDER("Education supervision order"),
    EMERGENCY_PROTECTION_ORDER("Emergency protection order"),
    OTHER("Other order under part 4 of the Children Act 1989");

    private final String label;

    OrderType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
