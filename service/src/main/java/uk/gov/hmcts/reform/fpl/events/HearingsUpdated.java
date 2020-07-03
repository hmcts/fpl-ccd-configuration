package uk.gov.hmcts.reform.fpl.events;

import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.Value;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;


@Value
@Generated
@EqualsAndHashCode(callSuper = true)
public class HearingsUpdated extends CallbackEvent {
    public HearingsUpdated(CallbackRequest callbackRequest) {
        super(callbackRequest);
    }
}
