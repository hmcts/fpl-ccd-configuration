package uk.gov.hmcts.reform.fpl.events;

import lombok.Value;
import uk.gov.hmcts.reform.fpl.model.CaseData;

@Value
public class CaseSubmittedEvent {
    private final CaseData caseData;

    public CaseSubmittedEvent(CaseData caseData) {
        this.caseData = caseData;
    }
}
