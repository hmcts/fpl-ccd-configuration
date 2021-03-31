package uk.gov.hmcts.reform.fpl.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TabUrlAnchor {
    START_APPLICATION("Start%20application"),
    VIEW_APPLICATION("View%20application"),
    PLACEMENT("Placement"),
    PAYMENTS("Payment history"),
    SENT_DOCUMENTS("Documents%20sent%20to%20parties"),
    NOTES("Notes"),
    EXPERT_REPORTS("Expert%20reports"),
    OVERVIEW("Overview"),
    HISTORY("History"),
    HEARINGS("Hearings"),
    DRAFT_ORDERS("Draft%20orders"),
    ORDERS("Orders"),
    PEOPLE("People%20in%20the%20case"),
    LEGAL_BASIS("Legal%20basis"),
    DOCUMENTS("Documents"),
    CORRESPONDENCE("Correspondence"),
    JUDICIAL_MESSAGES("Judicial%20messages"),
    C2("C2"),
    OTHER_APPLICATIONS("Other%20applications"),
    CONFIDENTIAL("Confidential%20information");

    private final String anchor;
}
