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
    @Deprecated
    private final String welsh;
    @Deprecated
    private final String interpreter;
    @Deprecated
    private final String intermediary;
    @Deprecated
    private final String welshDetails;
    @Deprecated
    private final String disabilityAssistance;
    @Deprecated
    private final String extraSecurityMeasures;
    @Deprecated
    private final String extraSecurityMeasuresDetails;
    @Deprecated
    private final String somethingElse;
    private final List<CourtServicesNeeded> whichCourtServices;
    private final String interpreterDetails;
    private final String intermediaryDetails;
    private final String disabilityAssistanceDetails;
    private final String separateWaitingRoomsDetails;
    private final String somethingElseDetails;
}
