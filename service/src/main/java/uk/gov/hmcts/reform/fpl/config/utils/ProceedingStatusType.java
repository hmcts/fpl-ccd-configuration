package uk.gov.hmcts.reform.fpl.config.utils;

public enum ProceedingStatusType {

    ONGOING("Ongoing"),
    PREVIOUS("Previous");

    private final String label;

    ProceedingStatusType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
