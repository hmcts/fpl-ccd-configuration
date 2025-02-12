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
     * This historical field is deprecated since DFPL-2303.
     * @deprecated (DFPL-2316, historical field)
     */
    @Deprecated(since = "DFPL-2316")
    private final String welsh;
    /**
     * This historical field is deprecated since DFPL-2303.
     * @deprecated (DFPL-2316, historical field)
     */
    @Deprecated(since = "DFPL-2316")
    private final String interpreter;
    /**
     * This historical field is deprecated since DFPL-2303.
     * @deprecated (DFPL-2316, historical field)
     */
    @Deprecated(since = "DFPL-2316")
    private final String intermediary;
    /**
     * This historical field is deprecated since DFPL-2303.
     * @deprecated (DFPL-2316, historical field)
     */
    @Deprecated(since = "DFPL-2316")
    private final String welshDetails;
    /**
     * This historical field is deprecated since DFPL-2303.
     * @deprecated (DFPL-2316, historical field)
     */
    @Deprecated(since = "DFPL-2316")
    private final String disabilityAssistance;
    /**
     * This historical field is deprecated since DFPL-2303.
     * @deprecated (DFPL-2316, historical field)
     */
    @Deprecated(since = "DFPL-2316")
    private final String extraSecurityMeasures;
    /**
     * This historical field is deprecated since DFPL-2303.
     * @deprecated (DFPL-2316, historical field)
     */
    @Deprecated(since = "DFPL-2316")
    private final String extraSecurityMeasuresDetails;
    /**
     * This historical field is deprecated since DFPL-2303.
     * @deprecated (DFPL-2316, historical field)
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
