package uk.gov.hmcts.reform.fpl.config.utils;

public enum GroundsForApplicationType {

    NOT_RECEIVING_CARE("Not receiving care that would be reasonably expected from a parent"),
    BEYOND_PARENTAL_CONTROL("Beyond parental control"),
    CONTESTED_INTERIM_CARE_ORDER("Contested interim care order"),
    URGENT_PRELIMINARY_CASE_MGMT_HEARING("Urgent preliminary case management hearing"),
    STANDARD_CASE_MANAGEMENT_HEARING("Standard case management hearing");

    private final String label;

    GroundsForApplicationType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
