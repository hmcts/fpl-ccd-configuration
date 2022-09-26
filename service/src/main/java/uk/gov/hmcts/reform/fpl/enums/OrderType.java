package uk.gov.hmcts.reform.fpl.enums;

import uk.gov.hmcts.reform.fpl.model.configuration.Language;

public enum OrderType {
    CARE_ORDER("Care order", "Gorchymyn Gofal"),
    INTERIM_CARE_ORDER("Interim care order", "Gorchymyn Gofal Dros Dro"),
    SUPERVISION_ORDER("Supervision order", "Gorchymyn Goruchwylio"),
    INTERIM_SUPERVISION_ORDER("Interim supervision order", "Gorchymyn Goruchwylio Dros Dro"),
    EDUCATION_SUPERVISION_ORDER("Education supervision order", "Gorchymyn goruchwylio addysg"),
    EMERGENCY_PROTECTION_ORDER("Emergency protection order", "Gorchymyn Diogelu Brys"),
    OTHER("Variation or discharge of care or supervision order",
        "Amrywio neu ddiddymu gorchymyn gofal neu oruchwyliaeth"),
    //todo - welsh translation
    CHILD_ASSESSMENT_ORDER("Child Assessment Order", "Child Assessment Order"),
    // TODO welshLabel
    SECURE_ACCOMMODATION_ORDER("Secure Accommodation order", ""),
    // TODO welshLabel
    REFUSE_CONTACT_WITH_CHILD("Authority to refuse contact with a child in care", ""),
    CONTACT_WITH_CHILD_IN_CARE("Contact with child in care", "Cyswllt â phlentyn mewn gofal");

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
