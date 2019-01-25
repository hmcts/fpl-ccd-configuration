package uk.gov.hmcts.reform.fpl.config.utils;

public enum YesNoDoNotKnowType {

    YES("Yes"),
    NO("No"),
    DO_NOT_KNOW("Don\'t know");

    private final String label;

    YesNoDoNotKnowType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
