package uk.gov.hmcts.reform.fpl.events.cmo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.events.CallbackEvent;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;

@Getter
@EqualsAndHashCode(callSuper = true)
public class CMOReadyToSealEvent extends CallbackEvent {
    private final HearingBooking hearing;

    public CMOReadyToSealEvent(CallbackRequest request, HearingBooking hearing) {
        super(request);
        this.hearing = hearing;
    }
}
