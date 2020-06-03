package uk.gov.hmcts.reform.fpl.events;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

public class PopulateStandardDirectionsOrderDatesEvent extends CallbackEvent {

    public PopulateStandardDirectionsOrderDatesEvent(CallbackRequest callbackRequest) {
        super(callbackRequest);
    }
}
