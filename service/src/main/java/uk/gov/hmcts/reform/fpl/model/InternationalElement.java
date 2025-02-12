package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class InternationalElement {
    /**
    * @deprecated (DFPL-2295)
    */
    @Deprecated(since = "DFPL-2295")
    private final String issues;
    /**
    * @deprecated (DFPL-2295)
    */
    @Deprecated(since = "DFPL-2295")
    private final String proceedings;
    /**
    * @deprecated (DFPL-2295)
    */
    @Deprecated(since = "DFPL-2295")
    private final String issuesReason;
    /**
    * @deprecated (DFPL-2295)
    */
    @Deprecated(since = "DFPL-2295")
    private final String possibleCarer;
    /**
    * @deprecated (DFPL-2295)
    */
    @Deprecated(since = "DFPL-2295")
    private final String proceedingsReason;
    /**
    * @deprecated (DFPL-2295)
    */
    @Deprecated(since = "DFPL-2295")
    private final String significantEvents;
    /**
    * @deprecated (DFPL-2295)
    */
    @Deprecated(since = "DFPL-2295")
    private final String possibleCarerReason;
    /**
    * @deprecated (DFPL-2295)
    */
    @Deprecated(since = "DFPL-2295")
    private final String significantEventsReason;
    /**
    * @deprecated (DFPL-2295)
    */
    @Deprecated(since = "DFPL-2295")
    private final String internationalAuthorityInvolvement;
    /**
    * @deprecated (DFPL-2295)
    */
    @Deprecated(since = "DFPL-2295")
    private final String internationalAuthorityInvolvementDetails;
    private final String whichCountriesInvolved;
    private final String outsideHagueConvention;
    private final String importantDetails;
}
