package uk.gov.hmcts.reform.fpl.model.docmosis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocmosisInternationalElement {
    private final String possibleCarer;
    private final String significantEvents;
    private final String proceedings;
    private final String internationalAuthorityInvolvement;
    private final String issues;
    private final String whichCountriesInvolved;
    private final String outsideHagueConvention;
    private final String importantDetails;
}
