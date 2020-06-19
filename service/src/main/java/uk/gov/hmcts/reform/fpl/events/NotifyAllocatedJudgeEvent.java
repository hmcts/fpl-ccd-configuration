package uk.gov.hmcts.reform.fpl.events;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

public class NotifyAllocatedJudgeEvent extends CallbackEvent {

    public NotifyAllocatedJudgeEvent(CallbackRequest callbackRequest) {
        super(callbackRequest);
    }
}
