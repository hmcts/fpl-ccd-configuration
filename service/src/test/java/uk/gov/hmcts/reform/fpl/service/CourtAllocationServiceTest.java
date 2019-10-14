package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.AllocationDecision;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;

class CourtAllocationServiceTest {

    private final CourtAllocationService courtAllocationService = new CourtAllocationService();

    //TODO: extract integration tests to unit test level.

    @Test
    void shouldAddYesToMissingAllocationDecision() {
        CaseData caseData = CaseData.builder()
            .allocationDecision(createAllocationDecision("test", "decision reason"))
            .allocationProposal(createAllocationDecision("proposal", "reason"))
            .build();

        AllocationDecision expectedDecision = AllocationDecision.builder()
            .proposal("test")
            .proposalReason("decision reason")
            .judgeLevelRadio("No")
            .allocationProposalPresent("Yes")
            .build();

        assertThat(courtAllocationService.createDecision(caseData)).isEqualTo(expectedDecision);
    }

    private AllocationDecision createAllocationDecision(String proposal, String reason) {
        return AllocationDecision.builder()
            .proposal(proposal)
            .proposalReason(reason)
            .build();
    }
}
