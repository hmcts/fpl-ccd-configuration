package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class FactorsParenting {
    private final String anythingElse;
    private final String alcoholDrugAbuse;
    private final String domesticViolence;
    private final String alcoholDrugAbuseReason;
    private final String domesticViolenceReason;
}
