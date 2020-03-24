package uk.gov.hmcts.reform.fpl.events;

import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

@Value
@EqualsAndHashCode(callSuper = true)
public class CaseManagementOrderReadyForPartyReviewEvent extends CallbackEvent {

    private final byte[] documentContents;

    public CaseManagementOrderReadyForPartyReviewEvent(CallbackRequest callbackRequest,
                                                       String authorization,
                                                       String userId,
                                                       byte[] documentContents) {
        super(callbackRequest, authorization, userId);
        this.documentContents = documentContents;
    }
}
