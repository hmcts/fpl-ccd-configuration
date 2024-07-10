package uk.gov.hmcts.reform.fpl.model.docmosis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocmosisRisks {
    private final String neglectDetails;
    private final String sexualAbuseDetails;
    private final String physicalHarmDetails;
    private final String emotionalHarmDetails;
    private final String physicalHarm;
    private final String emotionalHarm;
    private final String sexualAbuse;
    private final String neglect;
    private final String alcoholDrugAbuse;
    private final String domesticAbuse;
    private final String anythingElse;
}
