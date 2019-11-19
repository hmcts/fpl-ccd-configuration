package uk.gov.hmcts.reform.fpl.events;

import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

@Value
@EqualsAndHashCode(callSuper = true)
public class C21OrderEvent extends CallbackEvent {

    private final String mostRecentUploadedDocumentUrl;

    public C21OrderEvent(CallbackRequest callbackRequest, String authorization, String userId,
                         String mostRecentUploadedDocumentUrl) {
        super(callbackRequest, authorization, userId);
        this.mostRecentUploadedDocumentUrl = mostRecentUploadedDocumentUrl;
    }
}
