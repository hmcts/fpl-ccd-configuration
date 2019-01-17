package uk.gov.hmcts.reform.fpl.config.utils;

public enum HearingType {

    STANDARD_CASE_HEARING("Standard case management hearing"),
    URGENT_PRELIMINARY_HEARING("Urgent preliminary case management hearing"),
    EMERGENCY_PROTECTION_HEARING("Emergency protection order hearing"),
    CONTESTED_INTERIM_HEARING("Contested interim care order");

    private final String label;

    HearingType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
