package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.CourtServicesNeeded;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class HearingPreferences {
    @Deprecated(since = "DFPL-2316")
    private final String welsh;
    @Deprecated(since = "DFPL-2316")
    private final String interpreter;
    @Deprecated(since = "DFPL-2316")
    private final String intermediary;
    @Deprecated(since = "DFPL-2316")
    private final String welshDetails;
    @Deprecated(since = "DFPL-2316")
    private final String disabilityAssistance; 
    @Deprecated(since = "DFPL-2316")
    private final String extraSecurityMeasures;
    @Deprecated(since = "DFPL-2316")
    private final String extraSecurityMeasuresDetails;
    @Deprecated(since = "DFPL-2316")
    private final String somethingElse;
    private final List<CourtServicesNeeded> whichCourtServices;
    private final String interpreterDetails;
    private final String intermediaryDetails;
    private final String disabilityAssistanceDetails;
    private final String separateWaitingRoomsDetails;
    private final String somethingElseDetails;
}
