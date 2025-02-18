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
    private final String significantEvents;
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
    private final String internationalAuthorityInvolvement;
    /**
     * This historical field is deprecated since DFPL-2295.
     * @deprecated (DFPL-2295, historical field)
     */
    @Deprecated(since = "DFPL-2295")
    private final String issues;
    private final String whichCountriesInvolved;
    private final String outsideHagueConvention;
    private final String importantDetails;
}
