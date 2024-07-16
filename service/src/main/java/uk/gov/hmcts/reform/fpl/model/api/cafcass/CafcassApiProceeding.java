package uk.gov.hmcts.reform.fpl.model.api.cafcass;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CafcassApiProceeding {
    private String proceedingStatus;
    private String caseNumber;
    private String started;
    private String ended;
    private String ordersMade;
    private String judge;
    private String children;
    private String guardian;
    private boolean sameGuardianNeeded;
    private String sameGuardianDetails;
}
