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
    * @deprecated (DFPL-2295)
    */
    @Deprecated(since = "DFPL-2295")
    private final String possibleCarer;
    /**
    * @deprecated (DFPL-2295)
    */
    @Deprecated(since = "DFPL-2295")
    private final String significantEvents;
    /**
    * @deprecated (DFPL-2295)
    */
    @Deprecated(since = "DFPL-2295")
    private final String proceedings;
    /**
    * @deprecated (DFPL-2295)
    */
    @Deprecated(since = "DFPL-2295")
    private final String internationalAuthorityInvolvement;
    /**
    * @deprecated (DFPL-2295)
    */
    @Deprecated(since = "DFPL-2295")
    private final String issues;
    private final String whichCountriesInvolved;
    private final String outsideHagueConvention;
    private final String importantDetails;
}
