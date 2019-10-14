package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor

//TODO: remove class

public class AllocationProposal {
    private final String proposal;
    private final String proposalReason;
}
