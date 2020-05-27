package uk.gov.hmcts.reform.fpl.events;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.request.RequestData;

public class PopulateStandardDirectionsOrderDatesEvent extends CallbackEvent {

    public PopulateStandardDirectionsOrderDatesEvent(CallbackRequest callbackRequest, RequestData requestData) {
        super(callbackRequest, requestData);
    }
}
