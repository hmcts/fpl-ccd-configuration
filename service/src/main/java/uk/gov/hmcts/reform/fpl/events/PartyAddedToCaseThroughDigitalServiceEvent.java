package uk.gov.hmcts.reform.fpl.events;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

public class PartyAddedToCaseThroughDigitalServiceEvent extends CallbackEvent {

    public PartyAddedToCaseThroughDigitalServiceEvent(CallbackRequest callbackRequest, String authorization, String userId) {
        super(callbackRequest, authorization, userId);
    }
}
