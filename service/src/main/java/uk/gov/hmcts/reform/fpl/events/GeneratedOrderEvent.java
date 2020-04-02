package uk.gov.hmcts.reform.fpl.events;

import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.request.RequestData;

@Value
@EqualsAndHashCode(callSuper = true)
public class GeneratedOrderEvent extends CallbackEvent {

    private final String mostRecentUploadedDocumentUrl;
    private final byte[] documentContents;

    public GeneratedOrderEvent(CallbackRequest callbackRequest, RequestData requestData,
                         String mostRecentUploadedDocumentUrl, byte[] documentContents) {
        super(callbackRequest, requestData);
        this.mostRecentUploadedDocumentUrl = mostRecentUploadedDocumentUrl;
        this.documentContents = documentContents;
    }
}
