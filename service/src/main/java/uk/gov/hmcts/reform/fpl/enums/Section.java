package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.FplEvent;

@RequiredArgsConstructor
@Getter
public enum Section {
    APPLICANT(new String[]{"applicant", "solicitor"}, "applicant", FplEvent.APPLICANT),
    CHILDREN("children", "children", FplEvent.ENTER_CHILDREN),
    ORDERS("orders", "orders and directions needed", FplEvent.ORDERS_NEEDED),
    RESPONDENTS("respondent", "respondents", FplEvent.RESPONDENTS),
    GROUNDS("grounds", "grounds for the application", FplEvent.GROUNDS),
    HEARING("hearing", "hearing needed", FplEvent.HEARING_NEEDED),
    DOCUMENTS("document", "documents", FplEvent.DOCUMENTS),
    CASENAME("casename", "case name", FplEvent.CASENAME),
    ALLOCATION_PROPOSAL("allocationproposal", "allocation proposal", FplEvent.ALLOCATION_PROPOSAL);

    private final String[] errorKeys;
    private final String sectionHeaderName;
    private final FplEvent event;

    Section(String errorKey, String sectionHeaderName, FplEvent event) {
        this(new String[]{errorKey}, sectionHeaderName, event);
    }
}
