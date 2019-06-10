package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Proceeding {
    private final String onGoingProceeding;
    private final String proceedingStatus;
    private final String caseNumber;
    private final String started;
    private final String ended;
    private final String ordersMade;
    private final String judge;
    private final String children;
    private final String guardian;
    private final String sameGuardianNeeded;
    private final String sameGuardianDetails;

    @JsonCreator
    public Proceeding(@JsonProperty("onGoingProceeding") final String onGoingProceeding,
                      @JsonProperty("proceedingStatus") final String proceedingStatus,
                      @JsonProperty("caseNumber") final String caseNumber,
                      @JsonProperty("started") final String started,
                      @JsonProperty("ended") final String ended,
                      @JsonProperty("ordersMade") final String ordersMade,
                      @JsonProperty("judge") final String judge,
                      @JsonProperty("children") final String children,
                      @JsonProperty("guardian") final String guardian,
                      @JsonProperty("sameGuardianNeeded") final String sameGuardianNeeded,
                      @JsonProperty("sameGuardianDetails") final String sameGuardianDetails) {
        this.onGoingProceeding = onGoingProceeding;
        this.proceedingStatus = proceedingStatus;
        this.caseNumber = caseNumber;
        this.started = started;
        this.ended = ended;
        this.ordersMade = ordersMade;
        this.judge = judge;
        this.children = children;
        this.guardian = guardian;
        this.sameGuardianNeeded = sameGuardianNeeded;
        this.sameGuardianDetails = sameGuardianDetails;
    }
}
