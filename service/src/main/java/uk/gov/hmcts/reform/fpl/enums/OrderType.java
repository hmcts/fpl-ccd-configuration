package uk.gov.hmcts.reform.fpl.enums;

import uk.gov.hmcts.reform.fpl.model.configuration.Language;

public enum OrderType {
    CARE_ORDER("Care order", "Care order"),
    INTERIM_CARE_ORDER("Interim care order", "Interim care order"),
    SUPERVISION_ORDER("Supervision order", "Supervision order"),
    INTERIM_SUPERVISION_ORDER("Interim supervision order", "Interim supervision order"),
    EDUCATION_SUPERVISION_ORDER("Education supervision order", "Education supervision order"),
    EMERGENCY_PROTECTION_ORDER("Emergency protection order", "Emergency protection order"),
    OTHER("Variation or discharge of care or supervision order", "Variation or discharge of care or supervision order");

    private final String label;
    private final String welshLabel;

    OrderType(String label, String welshLabel) {
        this.label = label;
        this.welshLabel = welshLabel;
    }

    public String getLabel() {
        return label;
    }

    public String getLabel(Language language) {
        return language == Language.WELSH ? welshLabel : label;
    }
}
