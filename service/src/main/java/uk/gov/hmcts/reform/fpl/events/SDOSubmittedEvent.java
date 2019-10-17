package uk.gov.hmcts.reform.fpl.events;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

public class SDOSubmittedEvent extends CallbackEvent {

    public SDOSubmittedEvent(CallbackRequest callbackRequest, String authorization, String userId) {
        super(callbackRequest, authorization, userId);
    }
}
