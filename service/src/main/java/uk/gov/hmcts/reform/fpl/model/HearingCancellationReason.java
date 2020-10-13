package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HearingCancellationReason {
    private final String type;
    private final String reason;
}
