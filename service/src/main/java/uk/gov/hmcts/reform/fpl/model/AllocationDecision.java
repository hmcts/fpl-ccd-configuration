package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class AllocationDecision  {
    /*
        The below are called proposal as ideally we would like to reuse the allocationProposal complex directionType,
        but had some issues with labels so there is another complex directionType.
        The field could be renamed as tech debt.
     */
    private final String proposal;
    private final String proposalReason;
    private final String allocationProposalPresent;
}
