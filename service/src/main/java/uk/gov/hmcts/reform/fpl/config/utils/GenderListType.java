package uk.gov.hmcts.reform.fpl.config.utils;

public enum GenderListType {

    MALE("Male"),
    FEMALE("Female"),
    IDENTIFIED_IN_ANOTHER_WAY("They identify in another way");

    private final String label;

    GenderListType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
