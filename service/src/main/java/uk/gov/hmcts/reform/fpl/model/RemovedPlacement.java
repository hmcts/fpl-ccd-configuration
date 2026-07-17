package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class RemovedPlacement {
    @CCD(label = "Why is the placement application being removed?")
    String removalReason;
    @CCD(label = "Removed placement application")
    Placement placement;
}
