package uk.gov.hmcts.reform.fpl.events.order;

import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

@Data
public class GeneratedOrderEvent implements ManageOrdersEvent {
    private final CaseData caseData;
    private final DocumentReference orderDocument;
}
