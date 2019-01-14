package uk.gov.hmcts.reform.fpl.config.utils;

public enum EmergencyProtectionOrdersType {

    CHILD_WHEREABOUTS("Information on the whereabouts of the child"),
    ENTRY_PREMISES("Authorisation for entry of premises"),
    SEARCH_FOR_CHILD("Authorisation to search for another child on the premises"),
    OTHER("Other order under section 48 of the Children Act 1989");

    private final String label;

    EmergencyProtectionOrdersType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
