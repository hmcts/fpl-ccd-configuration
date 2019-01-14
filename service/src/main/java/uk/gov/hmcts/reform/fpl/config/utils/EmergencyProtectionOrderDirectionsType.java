package uk.gov.hmcts.reform.fpl.config.utils;

public enum EmergencyProtectionOrderDirectionsType {

    CONTACT_WITH_NAMED_PERSON("Contact with any named person"),
    CHILD_MEDICAL_ASSESSMENT("A medical or psychiatric examination, or another assessment of the child"),
    MEDICAL_PRACTITIONER("To be accompanied by a registered medical practitioner, nurse or midwife"),
    EXCLUSION_REQUIREMENT("An exclusion requirement"),
    OTHER("Other direction relating to an emergency protection order");

    private final String label;

    EmergencyProtectionOrderDirectionsType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
