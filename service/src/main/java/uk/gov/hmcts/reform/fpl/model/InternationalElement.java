package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class InternationalElement {
    /**
    * @deprecated
    */
    @Deprecated(since = "DFPL-2295")
    private final String issues;
    /**
    * @deprecated
    */
    @Deprecated(since = "DFPL-2295")
    private final String proceedings;
    /**
    * @deprecated
    */
    @Deprecated(since = "DFPL-2295")
    private final String issuesReason;
    /**
    * @deprecated
    */
    @Deprecated(since = "DFPL-2295")
    private final String possibleCarer;
    /**
    * @deprecated
    */
    @Deprecated(since = "DFPL-2295")
    private final String proceedingsReason;
    /**
    * @deprecated
    */
    @Deprecated(since = "DFPL-2295")
    private final String significantEvents;
    /**
    * @deprecated
    */
    @Deprecated(since = "DFPL-2295")
    private final String possibleCarerReason;
    /**
    * @deprecated
    */
    @Deprecated(since = "DFPL-2295")
    private final String significantEventsReason;
    /**
    * @deprecated
    */
    @Deprecated(since = "DFPL-2295")
    private final String internationalAuthorityInvolvement;
    /**
    * @deprecated
    */
    @Deprecated(since = "DFPL-2295")
    private final String internationalAuthorityInvolvementDetails;
    private final String whichCountriesInvolved;
    private final String outsideHagueConvention;
    private final String importantDetails;
}
