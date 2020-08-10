package uk.gov.hmcts.reform.fpl.events;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;


public class C2UploadedEvent extends CallbackEvent {
    private final C2DocumentBundle uploadedBundle;

    public C2UploadedEvent(CallbackRequest callbackRequest, C2DocumentBundle c2DocumentBundle) {
        super(callbackRequest);
        uploadedBundle = c2DocumentBundle;
    }

    public C2DocumentBundle getUploadedBundle() {
        return uploadedBundle;
    }
}
