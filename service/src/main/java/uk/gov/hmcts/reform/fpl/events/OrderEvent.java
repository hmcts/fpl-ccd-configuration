package uk.gov.hmcts.reform.fpl.events;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

@EqualsAndHashCode
@Getter
public class OrderEvent {

    private final CaseData caseData;
    private final DocumentReference orderDocument;

    protected OrderEvent(CaseData caseData, DocumentReference orderDocument) {
        this.caseData = caseData;
        this.orderDocument = orderDocument;
    }
}
