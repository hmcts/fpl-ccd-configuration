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
    /**
    * @deprecated
    */
    @Deprecated(since = "DFPL-2316")
    private final String welsh;
    /**
    * @deprecated
    */
    @Deprecated(since = "DFPL-2316")
    private final String interpreter;
    /**
    * @deprecated
    */
    @Deprecated(since = "DFPL-2316")
    private final String intermediary;
    /**
    * @deprecated
    */
    @Deprecated(since = "DFPL-2316")
    private final String welshDetails;
    /**
    * @deprecated
    */
    @Deprecated(since = "DFPL-2316")
    private final String disabilityAssistance;
    /**
    * @deprecated
    */    
    @Deprecated(since = "DFPL-2316")
    private final String extraSecurityMeasures;
    /**
    * @deprecated
    */
    @Deprecated(since = "DFPL-2316")
    private final String extraSecurityMeasuresDetails;
    /**
    * @deprecated
    */
    @Deprecated(since = "DFPL-2316")
    private final String somethingElse;
    private final List<CourtServicesNeeded> whichCourtServices;
    private final String interpreterDetails;
    private final String intermediaryDetails;
    private final String disabilityAssistanceDetails;
    private final String separateWaitingRoomsDetails;
    private final String somethingElseDetails;
}
