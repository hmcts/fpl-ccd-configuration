package uk.gov.hmcts.reform.fpl.enums;

public enum SectionType {
    APPLICANT("mainApplicant", "applicant"),
    CHILDREN("children", "children"),
    ORDERS("orders", "orders and directions needed"),
    GROUNDS("groundsForTheApplication", "grounds for the application"),
    HEARING("hearing", "hearing needed"),
    DOCUMENTS("documents", "documents"),
    CASENAME("caseName", "case name");

    private final String predicate;
    private final String sectionHeaderName;

    private SectionType(String predicate, String sectionHeaderName) {
        this.predicate = predicate;
        this.sectionHeaderName = sectionHeaderName;
    }

    public String getPredicate() {
        return predicate;
    }

    public String getSectionHeaderName() {
        return sectionHeaderName;
    }
}
