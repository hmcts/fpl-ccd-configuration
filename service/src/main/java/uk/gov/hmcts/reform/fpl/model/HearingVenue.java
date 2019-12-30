package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HearingVenue {
    private final String hearingVenueId;
    private final String venue;
    private final Address address;
}
