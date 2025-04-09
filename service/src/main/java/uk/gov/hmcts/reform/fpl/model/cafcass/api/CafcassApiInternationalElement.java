package uk.gov.hmcts.reform.fpl.model.cafcass.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CafcassApiInternationalElement {
    private Boolean possibleCarer;
    private String possibleCarerReason;
    private Boolean significantEvents;
    private String significantEventsReason;
    private Boolean issues;
    private String issuesReason;
    private Boolean proceedings;
    private String proceedingsReason;
    private Boolean internationalAuthorityInvolvement;
    private String internationalAuthorityInvolvementDetails;
}
