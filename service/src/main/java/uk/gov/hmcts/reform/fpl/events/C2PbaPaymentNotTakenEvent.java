package uk.gov.hmcts.reform.fpl.events;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

public class C2PbaPaymentNotTakenEvent extends CallbackEvent {

    public C2PbaPaymentNotTakenEvent(CallbackRequest callbackRequest) {
        super(callbackRequest);
    }
}
