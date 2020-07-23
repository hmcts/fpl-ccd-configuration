package uk.gov.hmcts.reform.fpl.events;

import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;

@Value
@EqualsAndHashCode(callSuper = true)
public class CaseManagementOrderIssuedEvent extends CallbackEvent {
    private final CaseManagementOrder cmo;

    public CaseManagementOrderIssuedEvent(CallbackRequest callbackRequest,
                                          CaseManagementOrder cmo) {
        super(callbackRequest);
        this.cmo = cmo;
    }
}
