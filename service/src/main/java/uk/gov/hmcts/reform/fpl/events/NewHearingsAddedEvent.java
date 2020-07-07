package uk.gov.hmcts.reform.fpl.events;

import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.Value;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;


@Value
@Generated
@EqualsAndHashCode(callSuper = true)
public class NewHearingsAddedEvent extends CallbackEvent {
    public NewHearingsAddedEvent(CallbackRequest callbackRequest) {
        super(callbackRequest);
    }
}
