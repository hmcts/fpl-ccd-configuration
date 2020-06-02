package uk.gov.hmcts.reform.fpl.events;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

public class PlacementApplicationEvent extends CallbackEvent {
    public PlacementApplicationEvent(CallbackRequest callbackRequest) {
        super(callbackRequest);
    }
}
