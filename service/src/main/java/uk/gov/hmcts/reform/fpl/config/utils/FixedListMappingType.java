package uk.gov.hmcts.reform.fpl.config.utils;

public enum FixedListMappingType {

    CARE_ORDER("Care order"),
    INTERIM_CARE_ORDER("Interim care order"),
    SUPERVISION_ORDER("Supervision order"),
    INTERIM_SUPERVISION_ORDER("Interim supervision order"),
    EDUCATION_SUPERVISION_ORDER("Education supervision order"),
    EMERGENCY_PROTECTION_ORDER("Emergency protection order"),
    OTHER("Other order under part 4 of the Children Act 1989"),
    CHILD_WHEREABOUTS("Information on the whereabouts of the child"),
    ENTRY_PREMISES("Authorisation for entry of premises"),
    SEARCH_FOR_ANOTHER_CHILD("Authorisation to search for another child on the premises"),
    EMERGENCY_PROTECTION_ORDER_OTHER("Other order under section 48 of the Children Act 1989"),
    CONTACT_WITH_NAMED_PERSON("Contact with any named person"),
    CHILD_MEDICAL_ASSESSMENT("A medical or psychiatric examination, or another assessment of the child"),
    MEDICAL_PRACTITIONER("To be accompanied by a registered medical practitioner, nurse or midwife"),
    EXCLUSION_REQUIREMENT("An exclusion requirement"),
    EMERGENCY_PROTECTION_DIRECTION_OTHER("Other direction relating to an emergency protection order");

    private final String label;

    FixedListMappingType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
