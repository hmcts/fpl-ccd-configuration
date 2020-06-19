package uk.gov.hmcts.reform.fpl.events;

import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

@Value
@EqualsAndHashCode(callSuper = true)
public class CaseManagementOrderReadyForPartyReviewEvent extends CallbackEvent {

    byte[] documentContents;

    public CaseManagementOrderReadyForPartyReviewEvent(CallbackRequest callbackRequest, byte[] documentContents) {
        super(callbackRequest);
        this.documentContents = documentContents;
    }
}
