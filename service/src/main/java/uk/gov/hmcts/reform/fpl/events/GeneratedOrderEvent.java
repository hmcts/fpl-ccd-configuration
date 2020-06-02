package uk.gov.hmcts.reform.fpl.events;

import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

@Value
@EqualsAndHashCode(callSuper = true)
public class GeneratedOrderEvent extends CallbackEvent {

    private final String mostRecentUploadedDocumentUrl;
    private final byte[] documentContents;

    public GeneratedOrderEvent(CallbackRequest callbackRequest, String mostRecentUploadedDocumentUrl,
                               byte[] documentContents) {
        super(callbackRequest);
        this.mostRecentUploadedDocumentUrl = mostRecentUploadedDocumentUrl;
        this.documentContents = documentContents;
    }
}
