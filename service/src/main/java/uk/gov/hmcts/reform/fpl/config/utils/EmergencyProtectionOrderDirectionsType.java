package uk.gov.hmcts.reform.fpl.config.utils;

import uk.gov.hmcts.reform.fpl.model.configuration.Language;

public enum EmergencyProtectionOrderDirectionsType {

    CONTACT_WITH_NAMED_PERSON("Contact with any named person",
        "Contact with any named person"),
    CHILD_MEDICAL_ASSESSMENT("A medical or psychiatric examination, or another assessment of the child",
        "A medical or psychiatric examination, or another assessment of the child"),
    MEDICAL_PRACTITIONER("To be accompanied by a registered medical practitioner, nurse or midwife",
        "To be accompanied by a registered medical practitioner, nurse or midwife"),
    EXCLUSION_REQUIREMENT("An exclusion requirement", "An exclusion requirement"),
    OTHER("Other direction relating to an emergency protection order",
        "Other direction relating to an emergency protection order");

    private final String label;
    private final String welshLabel;

    EmergencyProtectionOrderDirectionsType(String label, String welshLabel) {
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
