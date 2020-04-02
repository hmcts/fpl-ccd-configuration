package uk.gov.hmcts.reform.fpl.events;

import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.request.RequestData;

@Value
@EqualsAndHashCode(callSuper = true)
public class CaseManagementOrderReadyForPartyReviewEvent extends CallbackEvent {

    private final byte[] documentContents;

    public CaseManagementOrderReadyForPartyReviewEvent(CallbackRequest callbackRequest,
                                                       RequestData requestData,
                                                       byte[] documentContents) {
        super(callbackRequest, requestData);
        this.documentContents = documentContents;
    }
}
