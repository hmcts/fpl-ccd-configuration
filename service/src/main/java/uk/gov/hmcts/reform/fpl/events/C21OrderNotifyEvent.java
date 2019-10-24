package uk.gov.hmcts.reform.fpl.events;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

public class C21OrderNotifyEvent extends CallbackEvent {
    private final UserDetails userDetails;

    public C21OrderNotifyEvent(CallbackRequest callbackRequest, String authorization, String userId,
                               UserDetails userDetails) {
        super(callbackRequest, authorization, userId);
        this.userDetails = userDetails;
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }
}
