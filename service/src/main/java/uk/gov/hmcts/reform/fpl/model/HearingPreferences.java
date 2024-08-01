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
    private final String welsh;
    private final String interpreter;
    private final String intermediary;
    private final String welshDetails;
    private final String disabilityAssistance;
    private final String extraSecurityMeasures;
    private final String extraSecurityMeasuresDetails;
    private final String somethingElse;
    private final List<CourtServicesNeeded> whichCourtServices;
    private final String interpreterDetails;
    private final String intermediaryDetails;
    private final String disabilityAssistanceDetails;
    private final String separateWaitingRoomsDetails;
    private final String somethingElseDetails;
}
