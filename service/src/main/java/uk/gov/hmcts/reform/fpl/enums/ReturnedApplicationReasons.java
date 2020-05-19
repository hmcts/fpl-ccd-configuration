package uk.gov.hmcts.reform.fpl.enums;

public enum ReturnedApplicationReasons {
    INCORRECT("Application Incorrect"),
    INCOMPLETE("Application Incomplete"),
    CLARIFICATION_NEEDED("Clarification Needed");

    private final String label;

    ReturnedApplicationReasons(String label) {
        this.label = label;
    }
}
