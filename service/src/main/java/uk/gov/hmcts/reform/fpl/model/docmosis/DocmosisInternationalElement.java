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
    /**
    * @deprecated
    */
    @Deprecated(since = "DFPL-2295")
    private final String possibleCarer;
    /**
    * @deprecated
    */
    @Deprecated(since = "DFPL-2295")
    private final String significantEvents;
    /**
    * @deprecated
    */
    @Deprecated(since = "DFPL-2295")
    private final String proceedings;
    /**
    * @deprecated
    */
    @Deprecated(since = "DFPL-2295")
    private final String internationalAuthorityInvolvement;
    /**
    * @deprecated
    */
    @Deprecated(since = "DFPL-2295")
    private final String issues;
    private final String whichCountriesInvolved;
    private final String outsideHagueConvention;
    private final String importantDetails;
}
