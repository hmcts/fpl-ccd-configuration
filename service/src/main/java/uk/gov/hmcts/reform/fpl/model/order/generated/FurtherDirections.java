package uk.gov.hmcts.reform.fpl.model.order.generated;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FurtherDirections {
    private final String directionsNeeded;
    private final String directions;
}
