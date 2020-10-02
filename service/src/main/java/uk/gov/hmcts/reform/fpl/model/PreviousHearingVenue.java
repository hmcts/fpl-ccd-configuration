package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PreviousHearingVenue {
    private final String previousVenue;
    private final String usePreviousVenue;
    private final String newVenue;
    private final Address venueCustomAddress;
}
