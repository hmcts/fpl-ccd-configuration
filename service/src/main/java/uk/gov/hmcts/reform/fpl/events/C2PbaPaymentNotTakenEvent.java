package uk.gov.hmcts.reform.fpl.events;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

public class C2PbaPaymentNotTakenEvent extends CallbackEvent {

    public C2PbaPaymentNotTakenEvent(CallbackRequest callbackRequest, String authorization, String userId) {
        super(callbackRequest, authorization, userId);
    }
}
