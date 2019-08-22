package uk.gov.hmcts.reform.fpl.enums;

public enum Section {
    APPLICANT("mainApplicant", "applicant"),
    CHILDREN("children", "children"),
    ORDERS("orders", "orders and directions needed"),
    GROUNDS("groundsForTheApplication", "grounds for the application"),
    HEARING("hearing", "hearing needed"),
    DOCUMENTS("Document", "documents"),
    CASENAME("caseName", "case name");

    private final String errorKey;
    private final String sectionHeaderName;

    private Section(String errorKey, String sectionHeaderName) {
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
