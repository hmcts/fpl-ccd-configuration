package uk.gov.hmcts.reform.fpl.model.cafcass.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CafcassApiFactorsParenting {
    private Boolean alcoholDrugAbuse;
    private String alcoholDrugAbuseReason;
    private Boolean domesticViolence;
    private String domesticViolenceReason;
    private Boolean anythingElse;
    private String anythingElseReason;
}
