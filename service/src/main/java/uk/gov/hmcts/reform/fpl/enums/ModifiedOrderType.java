package uk.gov.hmcts.reform.fpl.enums;

public enum ModifiedOrderType {
    C11A("Application form"),
    URGENT_HEARING_ORDER("Urgent hearing order"),
    STANDARD_DIRECTION_ORDER("Standard direction order"),
    CASE_MANAGEMENT_ORDER("Case management order"),
    NOTICE_OF_PROCEEDINGS("Notice of Proceedings"),
    NOTICE_OF_HEARING("Notice of Hearing");

    private final String label;

    ModifiedOrderType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
