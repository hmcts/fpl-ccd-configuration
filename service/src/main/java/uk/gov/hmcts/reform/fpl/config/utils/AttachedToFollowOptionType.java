package uk.gov.hmcts.reform.fpl.config.utils;

public enum AttachedToFollowOptionType {

    ATTACHED("Attached"),
    TO_FOLLOW("To follow");

    private final String label;

    AttachedToFollowOptionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
