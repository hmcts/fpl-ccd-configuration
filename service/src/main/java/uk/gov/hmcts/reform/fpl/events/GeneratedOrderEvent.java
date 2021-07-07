package uk.gov.hmcts.reform.fpl.events;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

public class GeneratedOrderEvent extends OrderEvent {

    public GeneratedOrderEvent(CaseData caseData, DocumentReference documentReference) {
        super(caseData, documentReference);
    }
}
