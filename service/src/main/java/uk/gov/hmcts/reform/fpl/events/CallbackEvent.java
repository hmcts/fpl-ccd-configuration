package uk.gov.hmcts.reform.fpl.events;

import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

@EqualsAndHashCode
public class CallbackEvent {

    private final CallbackRequest callbackRequest;



     CallbackEvent(CallbackRequest callbackRequest) {
        this.callbackRequest = callbackRequest;
    }

    CallbackEvent(CallbackEvent callbackEvent) {
        this.callbackRequest = callbackEvent.callbackRequest;
    }

    public CallbackRequest getCallbackRequest() {
        return callbackRequest;
    }

}
