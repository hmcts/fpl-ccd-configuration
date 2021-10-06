package uk.gov.hmcts.reform.fpl.events.order;

import lombok.Value;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

@Value
public class GeneratedPlacementOrderEvent implements ManageOrdersEvent {

    CaseData caseData;
    DocumentReference orderDocument;
    DocumentReference orderNotificationDocument;

}
