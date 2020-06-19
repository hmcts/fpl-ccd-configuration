package uk.gov.hmcts.reform.fpl.events;

import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

@Value
@EqualsAndHashCode(callSuper = true)
public class CaseManagementOrderIssuedEvent extends CallbackEvent {
    private final byte[] documentContents;

    public CaseManagementOrderIssuedEvent(CallbackRequest callbackRequest, byte[] documentContents) {
        super(callbackRequest);
        this.documentContents = documentContents;
    }
}
