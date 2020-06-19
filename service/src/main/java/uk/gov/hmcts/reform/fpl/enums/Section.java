package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Section {
    APPLICANT(new String[] {"applicant", "solicitor"}, "applicant"),
    CHILDREN("children", "children"),
    ORDERS("orders", "orders and directions needed"),
    RESPONDENTS("respondent", "respondents"),
    REPRESENTATIVES("representative", "representatives"),
    GROUNDS("grounds", "grounds for the application"),
    HEARING("hearing", "hearing needed"),
    DOCUMENTS("document", "documents"),
    CASENAME("casename", "case name"),
    ALLOCATION_PROPOSAL("allocationproposal", "allocation proposal");

    private final String[] errorKeys;
    private final String sectionHeaderName;

    Section(String errorKey, String sectionHeaderName) {
        this(new String[] {errorKey}, sectionHeaderName);
    }
}
