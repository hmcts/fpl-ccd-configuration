package uk.gov.hmcts.reform.fpl.enums;

public enum TabLabel {
    START_APPLICATION("Start application"),
    VIEW_APPLICATION("View application"),
    PLACEMENT("Placement"),
    PAYMENTS("Payment history"),
    SENT_DOCUMENTS("Documents sent to parties"),
    NOTES("Notes"),
    EXPERT_REPORTS("Expert reports"),
    OVERVIEW("Overview"),
    HISTORY("History"),
    HEARINGS("Hearings"),
    DRAFT_ORDERS("Draft orders"),
    ORDERS("Orders"),
    PEOPLE("People in the case"),
    LEGAL_BASIS("Legal basis"),
    DOCUMENTS("Documents"),
    CORRESPONDENCE("Correspondence"),
    JUDICIAL_MESSAGES("Judicial messages"),
    C2("C2"),
    CONFIDENTIAL("Confidential information");

    private final String label;

    TabLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
