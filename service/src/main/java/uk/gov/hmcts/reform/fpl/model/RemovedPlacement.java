package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class RemovedPlacement {
    String removalReason;
    Placement removedPlacement;
}
