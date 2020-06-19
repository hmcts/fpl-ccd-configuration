package uk.gov.hmcts.reform.fpl.events;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

public class PopulateStandardDirectionsEvent extends CallbackEvent {

    public PopulateStandardDirectionsEvent(CallbackRequest callbackRequest) {
        super(callbackRequest);
    }
}
