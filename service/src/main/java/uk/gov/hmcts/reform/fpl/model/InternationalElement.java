package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class InternationalElement {
    /**
     * This historical field is deprecated since DFPL-2295.
     * @deprecated (DFPL-2295, historical field)
     */
    @Deprecated(since = "DFPL-2295")
    private final String issues;
    /**
     * This historical field is deprecated since DFPL-2295.
     * @deprecated (DFPL-2295, historical field)
     */
    @Deprecated(since = "DFPL-2295")
    private final String proceedings;
    /**
     * This historical field is deprecated since DFPL-2295.
     * @deprecated (DFPL-2295, historical field)
     */
    @Deprecated(since = "DFPL-2295")
    private final String issuesReason;
    /**
     * This historical field is deprecated since DFPL-2295.
     * @deprecated (DFPL-2295, historical field)
     */
    @Deprecated(since = "DFPL-2295")
    private final String possibleCarer;
    /**
     * This historical field is deprecated since DFPL-2295.
     * @deprecated (DFPL-2295, historical field)
     */
    @Deprecated(since = "DFPL-2295")
    private final String proceedingsReason;
    /**
     * This historical field is deprecated since DFPL-2295.
     * @deprecated (DFPL-2295, historical field)
     */
    @Deprecated(since = "DFPL-2295")
    private final String significantEvents;
    /**
     * This historical field is deprecated since DFPL-2295.
     * @deprecated (DFPL-2295, historical field)
     */
    @Deprecated(since = "DFPL-2295")
    private final String possibleCarerReason;
    /**
     * This historical field is deprecated since DFPL-2295.
     * @deprecated (DFPL-2295, historical field)
     */
    @Deprecated(since = "DFPL-2295")
    private final String significantEventsReason;
    /**
     * This historical field is deprecated since DFPL-2295.
     * @deprecated (DFPL-2295, historical field)
     */
    @Deprecated(since = "DFPL-2295")
    private final String internationalAuthorityInvolvement;
    /**
     * This historical field is deprecated since DFPL-2295.
     * @deprecated (DFPL-2295, historical field)
     */
    @Deprecated(since = "DFPL-2295")
    private final String internationalAuthorityInvolvementDetails;
    private final String whichCountriesInvolved;
    private final String outsideHagueConvention;
    private final String importantDetails;
}
