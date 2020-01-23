package uk.gov.hmcts.reform.fpl.events;

import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

@Value
@EqualsAndHashCode(callSuper = true) //NOSONAR
public class CaseManagementOrderRejectedEvent extends CallbackEvent {
    public CaseManagementOrderRejectedEvent(CallbackRequest callbackRequest, String authorization, String userId) {
        super(callbackRequest, authorization, userId);
    }
}
