package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Allocation {
    private final String proposal;
    private final String proposalReason;
    private final String allocationProposalPresent;
    private final String judgeLevelRadio;
}
