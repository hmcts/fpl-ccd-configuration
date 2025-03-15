package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import uk.gov.hmcts.reform.fpl.enums.ProceedingStatus;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
public class Proceeding {
    @Deprecated
    private final String onGoingProceeding;
    private final ProceedingStatus proceedingStatus;
    private final String caseNumber;
    private final String started;
    private final String ended;
    private final String ordersMade;
    private final String judge;
    private final String children;
    private final String guardian;
    private final YesNo sameGuardianNeeded;
    private final String sameGuardianDetails;
    @Deprecated
    private final List<Element<Proceeding>> additionalProceedings;
}
