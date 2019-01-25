package uk.gov.hmcts.reform.fpl.config.utils;

public enum HearingTimeFrameType {

    SAME_DAY("Same day"),
    WITHIN_2_DAYS("Within 2 days"),
    WITHIN_7_DAYS("Within 7 days"),
    WITHIN_12_DAYS("Within 12 days"),
    WITHIN_18_DAYS("Within 18 days");

    private final String label;

    HearingTimeFrameType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
