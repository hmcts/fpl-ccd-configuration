package uk.gov.hmcts.reform.fpl.events;

import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.Value;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;

@Value
@Generated
@EqualsAndHashCode(callSuper = true)
public class CaseManagementOrderRejectedEvent extends CallbackEvent {
    private final CaseManagementOrder cmo;

    public CaseManagementOrderRejectedEvent(CallbackRequest callbackRequest, CaseManagementOrder cmo) {
        super(callbackRequest);
        this.cmo = cmo;
    }
}
