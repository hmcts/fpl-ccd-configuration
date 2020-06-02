package uk.gov.hmcts.reform.fpl.events;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

public class NoticeOfProceedingsIssuedEvent extends CallbackEvent {

    public NoticeOfProceedingsIssuedEvent(CallbackRequest callbackRequest) {
        super(callbackRequest);
    }
}
