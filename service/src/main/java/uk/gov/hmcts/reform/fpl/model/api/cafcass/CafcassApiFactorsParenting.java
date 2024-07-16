package uk.gov.hmcts.reform.fpl.model.api.cafcass;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CafcassApiFactorsParenting {
    private boolean alcoholDrugAbuse;
    private String alcoholDrugAbuseReason;
    private boolean domesticViolence;
    private String domesticViolenceReason;
    private boolean anythingElse;
    private String anythingElseReason;
}
