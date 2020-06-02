package uk.gov.hmcts.reform.fpl.events;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

public class ReturnedCaseEvent extends CallbackEvent {
    public ReturnedCaseEvent(CallbackRequest callbackRequest) {
        super(callbackRequest);
    }
}
