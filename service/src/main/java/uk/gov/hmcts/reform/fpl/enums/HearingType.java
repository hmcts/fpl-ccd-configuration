package uk.gov.hmcts.reform.fpl.enums;

public enum HearingType {
    CASE_MANAGEMENT("Case management"),
    FURTHER_CASE_MANAGEMENT("Further case management"),
    ISSUE_RESOLUTION("Issue resolution"),
    FINAL("Final"),
    OTHER("Other");

    final String label;

    HearingType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
