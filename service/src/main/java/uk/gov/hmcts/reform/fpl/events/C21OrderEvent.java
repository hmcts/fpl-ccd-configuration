package uk.gov.hmcts.reform.fpl.events;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

@Data
@EqualsAndHashCode(callSuper = true)
public class C21OrderEvent extends CallbackEvent {

    private final C21OrderEventData c21OrderEventData;

    public C21OrderEvent(CallbackRequest callbackRequest, String authorization, String userId,
                         C21OrderEventData c21OrderEventData) {
        super(callbackRequest, authorization, userId);
        this.c21OrderEventData = c21OrderEventData;
    }

    @Data
    @Builder
    public static class C21OrderEventData {
        private final String documentUrl;
    }
}
