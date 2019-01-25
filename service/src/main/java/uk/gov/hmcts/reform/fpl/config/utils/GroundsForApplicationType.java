package uk.gov.hmcts.reform.fpl.config.utils;

public enum GroundsForApplicationType {

    NOT_RECEIVING_CARE("Not receiving care that would be reasonably expected from a parent"),
    BEYOND_PARENTAL_CONTROL("Beyond parental control");

    private final String label;

    GroundsForApplicationType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
