package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HearingVenue {
    private final String hearingVenueId;
    @JsonProperty(value = "title")
    private final String venue;
    private final Address address;
}
