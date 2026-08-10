package uk.gov.hmcts.reform.fpl.config.utils;

import uk.gov.hmcts.reform.fpl.model.configuration.Language;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "EmergencyProtectionOrderDirectionType", generate = true)
public enum EmergencyProtectionOrderDirectionsType {

    @CCD(label = "Contact with any named person")
    CONTACT_WITH_NAMED_PERSON("Contact with any named person",
        "Cyswllt ag unrhyw unigolyn a enwir"),
    @CCD(label = "A medical or psychiatric examination, or another assessment of the child")
    CHILD_MEDICAL_ASSESSMENT("A medical or psychiatric examination, or another assessment of the child",
        "Archwiliad meddygol neu seiciatrig, neu asesiad arall o'r plentyn"),
    @CCD(label = "To be accompanied by a registered medical practitioner, nurse or midwife")
    MEDICAL_PRACTITIONER("To be accompanied by a registered medical practitioner, nurse or midwife",
        "Bod gan yr unigolyn hawl i ddod ag ymarferydd meddygol cofrestredig, nyrs neu fydwraig efo nhw"),
    @CCD(label = "An exclusion requirement")
    EXCLUSION_REQUIREMENT("An exclusion requirement", "Gofyniad gwahardd"),
    @CCD(label = "Other direction relating to an emergency protection order")
    OTHER("Other direction relating to an emergency protection order",
        "Cyfarwyddyd arall sy'n ymwneud â gorchymyn diogelu brys");

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
