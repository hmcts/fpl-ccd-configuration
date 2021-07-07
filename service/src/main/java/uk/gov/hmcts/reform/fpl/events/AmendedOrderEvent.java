package uk.gov.hmcts.reform.fpl.events;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

public class AmendedOrderEvent extends OrderEvent {

    public AmendedOrderEvent(CaseData caseData, DocumentReference documentReference) {
        super(caseData, documentReference);
    }
}
