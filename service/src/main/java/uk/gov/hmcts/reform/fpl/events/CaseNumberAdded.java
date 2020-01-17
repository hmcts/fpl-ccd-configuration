package uk.gov.hmcts.reform.fpl.events;

import lombok.Value;
import uk.gov.hmcts.reform.fpl.model.CaseData;

@Value
public class CaseNumberAdded {
    private final CaseData caseData;

    public CaseNumberAdded(CaseData caseData) {
        this.caseData = caseData;
    }
}
