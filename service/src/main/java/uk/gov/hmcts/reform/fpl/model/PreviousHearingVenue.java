package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class PreviousHearingVenue {
    private final String previousVenue; //label showing full address on hearing details page
    private final String usePreviousVenue; //yes no radio
    private final String newVenue; //id - if selecting no, new hearing drop down
    private final Address newVenueCustomAddress; //if selecting no and selecting other
}
