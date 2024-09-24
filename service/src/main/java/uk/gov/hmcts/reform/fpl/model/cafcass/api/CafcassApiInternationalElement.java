package uk.gov.hmcts.reform.fpl.model.cafcass.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CafcassApiInternationalElement {
    private boolean possibleCarer;
    private String possibleCarerReason;
    private boolean significantEvents;
    private String significantEventsReason;
    private boolean issues;
    private String issuesReason;
    private boolean proceedings;
    private String proceedingsReason;
    private boolean internationalAuthorityInvolvement;
    private String internationalAuthorityInvolvementDetails;
}
