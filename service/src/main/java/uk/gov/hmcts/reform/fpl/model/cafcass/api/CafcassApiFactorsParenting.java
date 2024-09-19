package uk.gov.hmcts.reform.fpl.model.cafcass.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CafcassApiFactorsParenting {
    private boolean alcoholDrugAbuse;
    private String alcoholDrugAbuseReason;
    private boolean domesticViolence;
    private String domesticViolenceReason;
    private boolean anythingElse;
    private String anythingElseReason;
}
