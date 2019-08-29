package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class HearingPreferences {
    private final String welsh;
    private final String interpreter;
    private final String intermediary;
    private final String welshDetails;
    private final String interpreterDetails;
    private final String disabilityAssistance;
    private final String intermediaryDetails;
    private final String extraSecurityMeasures;
    private final String disabilityAssistanceDetails;
    private final String extraSecurityMeasuresDetails;
    private final String somethingElse;
    private final String somethingElseDetails;
}
