package uk.gov.hmcts.reform.fpl.events;

import lombok.Value;
import uk.gov.hmcts.reform.fpl.model.CaseData;

@Value
public class CaseNumberAddedEvent {
    private final CaseData caseData;

    public CaseNumberAddedEvent(CaseData caseData) {
        this.caseData = caseData;
    }
}
