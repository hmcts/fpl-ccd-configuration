package uk.gov.hmcts.reform.fpl.events;

import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;

@Value
@EqualsAndHashCode(callSuper = true)
public class CMOEvent extends CallbackEvent {
    private final DocmosisDocument document;

    public CMOEvent(CallbackRequest callbackRequest, String authorization, String userId,
                    DocmosisDocument document) {
        super(callbackRequest, authorization, userId);
        this.document = document;
    }
}
