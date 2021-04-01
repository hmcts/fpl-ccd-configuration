package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class HearingVenue {
    private final String hearingVenueId;
    private final String venue;
    private final Address address;
}
