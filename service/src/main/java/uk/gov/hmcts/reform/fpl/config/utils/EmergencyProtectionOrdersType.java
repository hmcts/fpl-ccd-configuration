package uk.gov.hmcts.reform.fpl.config.utils;

import uk.gov.hmcts.reform.fpl.model.configuration.Language;

public enum EmergencyProtectionOrdersType {

    CHILD_WHEREABOUTS("Information on the whereabouts of the child",
        "Gwybodaeth am leoliad y plentyn"),
    ENTRY_PREMISES("Authorisation for entry of premises",
        "Awdurdod i gael mynediad i fangre"),
    SEARCH_FOR_CHILD("Authorisation to search for another child on the premises",
        "Awdurdod i chwilio am blentyn arall ar y safle"),
    OTHER("Other order under section 48 of the Children Act 1989",
        "Gorchymyn arall o dan adran 48 o Ddeddf Plant 1989");

    private final String label;
    private final String welshLabel;

    EmergencyProtectionOrdersType(String label, String welshLabel) {
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
