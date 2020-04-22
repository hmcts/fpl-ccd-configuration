package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DocmosisFactorsParenting {
    private final String anythingElse;
    private final String alcoholDrugAbuseDetails;
    private final String domesticViolenceDetails;
}
