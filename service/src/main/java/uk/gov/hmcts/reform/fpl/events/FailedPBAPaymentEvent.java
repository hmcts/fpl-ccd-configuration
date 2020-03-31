package uk.gov.hmcts.reform.fpl.events;

import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.enums.ApplicationType;
import uk.gov.hmcts.reform.fpl.request.RequestData;

@Value
@EqualsAndHashCode(callSuper = true)
public class FailedPBAPaymentEvent extends CallbackEvent {

    private final ApplicationType applicationType;

    public FailedPBAPaymentEvent(CallbackRequest callbackRequest, RequestData requestData,
                                 ApplicationType applicationType) {
        super(callbackRequest, requestData);
        this.applicationType = applicationType;
    }
}
