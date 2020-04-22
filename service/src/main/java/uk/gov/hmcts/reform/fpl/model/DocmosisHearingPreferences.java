package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DocmosisHearingPreferences {
    private final String interpreter;
    private final String welshDetails;
    private final String intermediary;
    private final String disabilityAssistance;
    private final String extraSecurityMeasures;
    private final String somethingElse;
}
