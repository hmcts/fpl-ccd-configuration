package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DocmosisRisks {
    private final String neglectDetails;
    private final String sexualAbuseDetails;
    private final String physicalHarmDetails;
    private final String emotionalHarmDetails;
}
