package uk.gov.hmcts.reform.fpl.events;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

public class CaseDataChanged extends CallbackEvent {

    public CaseDataChanged(CallbackRequest callbackRequest) {
        super(callbackRequest);
    }
}
