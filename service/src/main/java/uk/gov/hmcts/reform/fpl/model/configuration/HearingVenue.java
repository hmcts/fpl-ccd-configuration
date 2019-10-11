package uk.gov.hmcts.reform.fpl.model.configuration;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HearingVenue {
    private final int hearingVenueId;
    private final String title;
}
