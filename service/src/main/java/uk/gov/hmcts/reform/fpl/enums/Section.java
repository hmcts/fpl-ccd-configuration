package uk.gov.hmcts.reform.fpl.enums;

public enum Section {
    APPLICANT("applicant", "applicant"),
    CHILDREN("children", "children"),
    ORDERS("orders", "orders and directions needed"),
    RESPONDENTS("respondent", "respondents"),
    GROUNDS("grounds", "grounds for the application"),
    HEARING("hearing", "hearing needed"),
    DOCUMENTS("document", "documents"),
    CASENAME("casename", "case name");

    private final String errorKey;
    private final String sectionHeaderName;

    Section(String errorKey, String sectionHeaderName) {
        this.errorKey = errorKey;
        this.sectionHeaderName = sectionHeaderName;
    }

    public String getErrorKey() {
        return errorKey;
    }

    public String getSectionHeaderName() {
        return sectionHeaderName;
    }
}
