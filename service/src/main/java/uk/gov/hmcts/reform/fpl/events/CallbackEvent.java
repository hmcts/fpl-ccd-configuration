package uk.gov.hmcts.reform.fpl.events;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.request.RequestData;

public class CallbackEvent {

    private final CallbackRequest callbackRequest;
    private final RequestData requestData;

    CallbackEvent(CallbackRequest callbackRequest, RequestData requestData) {
        this.callbackRequest = callbackRequest;
        this.requestData = requestData;
    }

    public CallbackRequest getCallbackRequest() {
        return callbackRequest;
    }

    public String getAuthorization() {
        return requestData.authorisation();
    }

    public String getUserId() {
        return requestData.userId();
    }
}
