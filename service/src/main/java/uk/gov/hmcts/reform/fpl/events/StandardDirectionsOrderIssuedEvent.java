package uk.gov.hmcts.reform.fpl.events;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

public class StandardDirectionsOrderIssuedEvent extends CallbackEvent {

    public StandardDirectionsOrderIssuedEvent(CallbackRequest callbackRequest) {
        super(callbackRequest);
    }
}
