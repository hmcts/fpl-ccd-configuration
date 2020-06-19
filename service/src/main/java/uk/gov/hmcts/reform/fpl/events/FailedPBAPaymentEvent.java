package uk.gov.hmcts.reform.fpl.events;

import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.enums.ApplicationType;

@Value
@EqualsAndHashCode(callSuper = true)
public class FailedPBAPaymentEvent extends CallbackEvent {

    private final ApplicationType applicationType;

    public FailedPBAPaymentEvent(CallbackRequest callbackRequest, ApplicationType applicationType) {
        super(callbackRequest);
        this.applicationType = applicationType;
    }

    public FailedPBAPaymentEvent(CallbackEvent originalEvent, ApplicationType applicationType) {
        super(originalEvent);
        this.applicationType = applicationType;
    }
}
