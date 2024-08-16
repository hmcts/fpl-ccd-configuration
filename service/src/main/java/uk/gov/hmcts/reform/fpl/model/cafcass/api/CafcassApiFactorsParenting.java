package uk.gov.hmcts.reform.fpl.model.cafcass.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CafcassApiFactorsParenting {
    private boolean alcoholDrugAbuse;
    private String alcoholDrugAbuseReason;
    private boolean domesticViolence;
    private String domesticViolenceReason;
    private boolean anythingElse;
    private String anythingElseReason;
}
