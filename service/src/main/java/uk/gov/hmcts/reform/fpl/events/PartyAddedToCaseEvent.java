package uk.gov.hmcts.reform.fpl.events;

import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.model.Representative;

import java.util.List;

@Value
@EqualsAndHashCode(callSuper = true)
public class PartyAddedToCaseEvent extends CallbackEvent {
    private final List<Representative> representativesToNotify;

    public PartyAddedToCaseEvent(CallbackRequest callbackRequest, String authorization, String userId,
                                 List<Representative> representativesToNotify) {
        super(callbackRequest, authorization, userId);
        this.representativesToNotify = representativesToNotify;
    }
}
