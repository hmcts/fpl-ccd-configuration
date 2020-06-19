package uk.gov.hmcts.reform.fpl.events;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

public class NotifyGatekeepersEvent extends CallbackEvent {

    public NotifyGatekeepersEvent(CallbackRequest callbackRequest) {
        super(callbackRequest);
    }
}
