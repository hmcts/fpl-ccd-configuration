package uk.gov.hmcts.reform.fpl.events;

import lombok.Value;
import uk.gov.hmcts.reform.fpl.model.CaseData;

@Value
public class AddCaseNumberEvent {
    private final CaseData caseData;

    public AddCaseNumberEvent(CaseData caseData) {
        this.caseData = caseData;
    }
}
