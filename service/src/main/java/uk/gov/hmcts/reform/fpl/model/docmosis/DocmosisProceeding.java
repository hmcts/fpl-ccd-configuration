package uk.gov.hmcts.reform.fpl.model.docmosis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocmosisProceeding {
    private final String onGoingProceeding;
    private final String proceedingStatus;
    private final String caseNumber;
    private final String started;
    private final String ended;
    private final String ordersMade;
    private final String judge;
    private final String children;
    private final String guardian;
    private final String sameGuardianDetails;
}
