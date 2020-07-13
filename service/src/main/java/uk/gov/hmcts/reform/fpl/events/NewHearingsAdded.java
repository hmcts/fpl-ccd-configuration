package uk.gov.hmcts.reform.fpl.events;

import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.Value;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

@Value
@Generated
@EqualsAndHashCode(callSuper = true)
public class NewHearingsAdded extends CallbackEvent {

    private List<Element<HearingBooking>> newHearings;

    public NewHearingsAdded(CallbackRequest callbackRequest, List<Element<HearingBooking>> newHearings) {
        super(callbackRequest);
        this.newHearings = newHearings;
    }
}
