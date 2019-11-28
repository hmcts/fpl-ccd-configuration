package uk.gov.hmcts.reform.fpl.events;

import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

@Value
@EqualsAndHashCode(callSuper = true)
public class CMOIssuedEvent extends CallbackEvent {
    private final String documentUrl;

    public CMOIssuedEvent(CallbackRequest callbackRequest, String authorization, String userId,
                          String documentUrl) {
        super(callbackRequest, authorization, userId);
        this.documentUrl = documentUrl;
    }
}
