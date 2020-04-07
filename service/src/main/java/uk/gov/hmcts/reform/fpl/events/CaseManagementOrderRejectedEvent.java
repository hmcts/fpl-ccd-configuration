package uk.gov.hmcts.reform.fpl.events;

import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.Value;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.request.RequestData;

@Value
@Generated
@EqualsAndHashCode(callSuper = true)
public class CaseManagementOrderRejectedEvent extends CallbackEvent {
    public CaseManagementOrderRejectedEvent(CallbackRequest callbackRequest, RequestData requestData) {
        super(callbackRequest, requestData);
    }
}
