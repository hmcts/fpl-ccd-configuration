package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class InternationalElement {
    @Deprecated(since = "DFPL-2295")
    private final String issues;
    @Deprecated(since = "DFPL-2295")
    private final String proceedings;
    @Deprecated(since = "DFPL-2295")
    private final String issuesReason;
    @Deprecated(since = "DFPL-2295")
    private final String possibleCarer;
    @Deprecated(since = "DFPL-2295")
    private final String proceedingsReason;
    @Deprecated(since = "DFPL-2295")
    private final String significantEvents;
    @Deprecated(since = "DFPL-2295")
    private final String possibleCarerReason;
    @Deprecated(since = "DFPL-2295")
    private final String significantEventsReason;
    @Deprecated(since = "DFPL-2295")
    private final String internationalAuthorityInvolvement;
    @Deprecated(since = "DFPL-2295")
    private final String internationalAuthorityInvolvementDetails;
    private final String whichCountriesInvolved;
    private final String outsideHagueConvention;
    private final String importantDetails;
}
