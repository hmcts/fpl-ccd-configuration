package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Section {
    APPLICANT(new String[] {"applicant", "solicitor"}, "applicant","Enter applicant", "enterApplicant"),
    CHILDREN("children", "children","Enter child", "enterChildren"),
    ORDERS("orders", "orders and directions needed","Orders and direction needed", "ordersNeeded"),
    RESPONDENTS("respondent", "respondents", "Respondents", "enterRespondents"),
    GROUNDS("grounds", "grounds for the application", "Grounds for application","enterGrounds"),
    HEARING("hearing", "hearing needed","Hearing needed","hearingNeeded"),
    DOCUMENTS("document", "documents", "Documents","uploadDocuments"),
    CASENAME("casename", "case name", "Case name", "changeCaseName"),
    ALLOCATION_PROPOSAL("allocationproposal", "allocation proposal"," Allocation proposal","otherProposal");

    private final String[] errorKeys;
    private final String sectionHeaderName;
    private final String label;
    private final String event;

    Section(String errorKey, String sectionHeaderName, String label, String event) {
        this(new String[] {errorKey}, sectionHeaderName, label, event);
    }
}
