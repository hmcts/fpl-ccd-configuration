package uk.gov.hmcts.reform.fpl.events;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

public class PartyAddedToCaseEvent extends CallbackEvent {

    public PartyAddedToCaseEvent(CallbackRequest callbackRequest, String authorization, String userId) {
        super(callbackRequest, authorization, userId);
    }
}
