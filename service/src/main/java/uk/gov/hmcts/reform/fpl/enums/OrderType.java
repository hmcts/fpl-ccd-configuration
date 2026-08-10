package uk.gov.hmcts.reform.fpl.enums;

import uk.gov.hmcts.reform.fpl.model.configuration.Language;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum OrderType {
    @CCD(label = "Care order")
    CARE_ORDER("Care order", "Gorchymyn Gofal"),
    @CCD(label = "Interim care order")
    INTERIM_CARE_ORDER("Interim care order", "Gorchymyn Gofal Dros Dro"),
    @CCD(label = "Supervision order")
    SUPERVISION_ORDER("Supervision order", "Gorchymyn Goruchwylio"),
    @CCD(label = "Interim supervision order")
    INTERIM_SUPERVISION_ORDER("Interim supervision order", "Gorchymyn Goruchwylio Dros Dro"),
    @CCD(label = "Education supervision order")
    EDUCATION_SUPERVISION_ORDER("Education supervision order", "Gorchymyn goruchwylio addysg"),
    @CCD(label = "Emergency protection order")
    EMERGENCY_PROTECTION_ORDER("Emergency protection order", "Gorchymyn Diogelu Brys"),
    @CCD(label = "Variation of supervision order or discharge of care order")
    OTHER("Variation or discharge of care or supervision order",
        "Amrywio neu ddiddymu gorchymyn gofal neu oruchwyliaeth"),
    //todo - welsh translation
    @CCD(label = "Child Assessment Order")
    CHILD_ASSESSMENT_ORDER("Child Assessment Order", "Child Assessment Order"),
    // TODO welshLabel
    @CCD(label = "Secure accommodation order")
    SECURE_ACCOMMODATION_ORDER("Secure Accommodation order", ""),
    // TODO welshLabel
    @CCD(label = "Authority to refuse contact with a child in care")
    REFUSE_CONTACT_WITH_CHILD("Authority to refuse contact with a child in care", ""),
    // TODO welshLabel
    @CCD(label = "Child Recovery Order")
    CHILD_RECOVERY_ORDER("Child Recovery Order", ""),
    @CCD(label = "Contact with child in care")
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
