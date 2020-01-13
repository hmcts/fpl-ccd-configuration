package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FurtherDirections {
    private final String directionsNeeded;
    private final String directions;
}
