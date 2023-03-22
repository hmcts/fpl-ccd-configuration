package uk.gov.hmcts.reform.fpl.events;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

@Value
@Builder(toBuilder = true)
public class ListAdminEvent {

    CaseData caseData;
    DocumentReference order;
    String sendToAdminReason;
    boolean isSentToAdmin;

}
