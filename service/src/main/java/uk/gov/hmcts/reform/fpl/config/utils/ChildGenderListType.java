package uk.gov.hmcts.reform.fpl.config.utils;

public enum ChildGenderListType {

    GIRL("Girl"),
    BOY("Boy"),
    IDENTIFIED_IN_ANOTHER_WAY("They identify in another way");

    private final String label;

    ChildGenderListType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
