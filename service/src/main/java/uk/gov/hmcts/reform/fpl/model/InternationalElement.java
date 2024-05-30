package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class InternationalElement {
    private final String issues;
    private final String proceedings;
    private final String issuesReason;
    private final String possibleCarer;
    private final String proceedingsReason;
    private final String significantEvents;
    private final String possibleCarerReason;
    private final String significantEventsReason;
    private final String internationalAuthorityInvolvement;
    private final String internationalAuthorityInvolvementDetails;
    private final String whichCountriesInvolved;
    private final String outsideHagueConvention;
    private final String importantDetails;
}
