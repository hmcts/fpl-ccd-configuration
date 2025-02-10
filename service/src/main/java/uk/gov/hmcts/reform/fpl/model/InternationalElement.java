package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class InternationalElement {
    @Deprecated
    private final String issues;
    @Deprecated
    private final String proceedings;
    @Deprecated
    private final String issuesReason;
    @Deprecated
    private final String possibleCarer;
    @Deprecated
    private final String proceedingsReason;
    @Deprecated
    private final String significantEvents;
    @Deprecated
    private final String possibleCarerReason;
    @Deprecated
    private final String significantEventsReason;
    @Deprecated
    private final String internationalAuthorityInvolvement;
    @Deprecated
    private final String internationalAuthorityInvolvementDetails;
    private final String whichCountriesInvolved;
    private final String outsideHagueConvention;
    private final String importantDetails;
}
