package uk.gov.hmcts.reform.fpl.events;

import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

@Value
@EqualsAndHashCode(callSuper = true)
public class FailedPBAPaymentEvent extends CallbackEvent {

    private final String applicationType;

    public FailedPBAPaymentEvent(CallbackRequest callbackRequest, String authorization, String userId,
                                 String applicationType) {
        super(callbackRequest, authorization, userId);
        this.applicationType = applicationType;
    }
}
