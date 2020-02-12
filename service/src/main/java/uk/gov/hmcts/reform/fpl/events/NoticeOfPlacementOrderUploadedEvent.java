package uk.gov.hmcts.reform.fpl.events;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

public class NoticeOfPlacementOrderUploadedEvent extends CallbackEvent {

    public NoticeOfPlacementOrderUploadedEvent(CallbackRequest callbackRequest, String authorization, String userId) {
        super(callbackRequest, authorization, userId);
    }
}
