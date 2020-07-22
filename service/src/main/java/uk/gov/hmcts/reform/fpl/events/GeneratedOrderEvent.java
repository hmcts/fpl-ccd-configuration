package uk.gov.hmcts.reform.fpl.events;

import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

@Value
@EqualsAndHashCode(callSuper = true)
public class GeneratedOrderEvent extends CallbackEvent {

    private final DocumentReference documentReference;

    public GeneratedOrderEvent(CallbackRequest callbackRequest, DocumentReference documentReference) {
        super(callbackRequest);
        this.documentReference = documentReference;
    }
}
