package uk.gov.hmcts.reform.fpl.events;

import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

@Value
@EqualsAndHashCode(callSuper = true)
public class CMOEvent extends CallbackEvent {
    public CMOEvent(CallbackRequest callbackRequest, String authorization, String userId) {
        super(callbackRequest, authorization, userId);
    }
}
