package uk.gov.hmcts.reform.fpl.events;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.request.RequestData;

public class CallbackEvent {

    private final CallbackRequest callbackRequest;
    private final String authorisation;
    private final String userId;

    CallbackEvent(CallbackRequest callbackRequest, RequestData requestData) {
        this.callbackRequest = callbackRequest;
        this.authorisation = requestData.authorisation();
        this.userId = requestData.userId();
    }

    public CallbackRequest getCallbackRequest() {
        return callbackRequest;
    }

    public String getAuthorization() {
        return authorisation;
    }

    public String getUserId() {
        return userId;
    }
}
