package uk.gov.hmcts.reform.fpl.model.docmosis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocmosisHearingPreferences {
    private final String interpreter;
    private final String welshDetails;
    private final String intermediary;
    private final String disabilityAssistance;
    private final String extraSecurityMeasures;
    private final String somethingElse;
}
