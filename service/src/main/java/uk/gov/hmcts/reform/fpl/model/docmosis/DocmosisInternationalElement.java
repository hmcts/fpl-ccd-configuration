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
    @Deprecated
    private final String possibleCarer;
    @Deprecated
    private final String significantEvents;
    @Deprecated
    private final String proceedings;
    @Deprecated
    private final String internationalAuthorityInvolvement;
    @Deprecated
    private final String issues;
    private final String whichCountriesInvolved;
    private final String outsideHagueConvention;
    private final String importantDetails;
}
