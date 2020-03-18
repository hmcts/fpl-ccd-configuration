package uk.gov.hmcts.reform.fpl.config.utils;

import uk.gov.hmcts.ccd.sdk.types.HasLabel;

public enum EmergencyProtectionOrderDirectionsType implements HasLabel {

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
