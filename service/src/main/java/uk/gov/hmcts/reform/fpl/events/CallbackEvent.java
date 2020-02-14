package uk.gov.hmcts.reform.fpl.events;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

public class CallbackEvent {

    //TODO TECHDEBT use RequestData here so that we don't pass authorization/user id into every event
    private final CallbackRequest callbackRequest;
    private final String authorization;
    private final String userId;

    CallbackEvent(CallbackRequest callbackRequest, String authorization, String userId) {
        this.callbackRequest = callbackRequest;
        this.authorization = authorization;
        this.userId = userId;
    }

    public CallbackRequest getCallbackRequest() {
        return callbackRequest;
    }

    public String getAuthorization() {
        return authorization;
    }

    public String getUserId() {
        return userId;
    }
}
