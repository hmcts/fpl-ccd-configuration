package uk.gov.hmcts.reform.fpl.events;

import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

@EqualsAndHashCode
public class CallbackEvent {

    private final CallbackRequest callbackRequest;

    protected CallbackEvent(CallbackRequest callbackRequest) {
        this.callbackRequest = callbackRequest;
    }

    protected CallbackEvent(CallbackEvent callbackEvent) {
        this.callbackRequest = callbackEvent.callbackRequest;
    }

    public CallbackRequest getCallbackRequest() {
        return callbackRequest;
    }

}
