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
    * @deprecated (DFPL-2316)
    */
    @Deprecated(since = "DFPL-2316")
    private final String welsh;
    /**
    * @deprecated (DFPL-2316)
    */
    @Deprecated(since = "DFPL-2316")
    private final String interpreter;
    /**
    * @deprecated (DFPL-2316)
    */
    @Deprecated(since = "DFPL-2316")
    private final String intermediary;
    /**
    * @deprecated (DFPL-2316)
    */
    @Deprecated(since = "DFPL-2316")
    private final String welshDetails;
    /**
    * @deprecated (DFPL-2316)
    */
    @Deprecated(since = "DFPL-2316")
    private final String disabilityAssistance;
    /**
    * @deprecated (DFPL-2316)
    */
    @Deprecated(since = "DFPL-2316")
    private final String extraSecurityMeasures;
    /**
    * @deprecated (DFPL-2316)
    */
    @Deprecated(since = "DFPL-2316")
    private final String extraSecurityMeasuresDetails;
    /**
    * @deprecated (DFPL-2316)
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
