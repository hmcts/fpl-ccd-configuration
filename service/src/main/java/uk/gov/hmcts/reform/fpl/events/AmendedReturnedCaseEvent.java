package uk.gov.hmcts.reform.fpl.events;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

public class AmendedReturnedCaseEvent extends CallbackEvent {
    public AmendedReturnedCaseEvent(CallbackRequest callbackRequest) {
        super(callbackRequest);
    }
}
