package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;

class CourtLevelAllocationServiceTest {

    private final CourtLevelAllocationService courtAllocationService = new CourtLevelAllocationService();

    @Test
    void shouldAddYesToMissingAllocationDecision() {
        CaseData caseData = CaseData.builder()
            .allocationDecision(createAllocationDecision("test", "decision reason"))
            .allocationProposal(createAllocationDecision("proposal", "reason"))
            .build();

        Allocation expectedDecision = Allocation.builder()
            .proposal("test")
            .proposalReason("decision reason")
            .judgeLevelRadio("No")
            .allocationProposalPresent("Yes")
            .build();

        assertThat(courtAllocationService.createDecision(caseData)).isEqualTo(expectedDecision);
    }

    @Test
    void shouldAddNoToMissingAllocationDecision() {
        CaseData caseData = CaseData.builder()
            .build();

        Allocation expectedDecision = Allocation.builder()
            .allocationProposalPresent("No")
            .build();

        assertThat(courtAllocationService.createDecision(caseData)).isEqualTo(expectedDecision);
    }

    @Test
    void shouldAddNullToJudgeLevelRadio() {
        CaseData caseData = CaseData.builder()
            .allocationProposal(createAllocationDecision("proposal", "reason"))
            .build();

        Allocation expectedDecision = Allocation.builder()
            .judgeLevelRadio(null)
            .allocationProposalPresent("Yes")
            .build();

        assertThat(courtAllocationService.createDecision(caseData)).isEqualTo(expectedDecision);
    }

    @Test
    void shouldAddYesToJudgeLevelRadio() {
        CaseData caseData = CaseData.builder()
            .allocationDecision(createAllocationDecision("proposal", "reason"))
            .allocationProposal(createAllocationDecision("proposal", "reason"))
            .build();

        Allocation expectedDecision = Allocation.builder()
            .judgeLevelRadio("Yes")
            .proposal("proposal")
            .proposalReason("reason")
            .allocationProposalPresent("Yes")
            .build();

        assertThat(courtAllocationService.createDecision(caseData)).isEqualTo(expectedDecision);
    }

    @Test
    void shouldAddNoToJudgeLevelRadio() {
        CaseData caseData = CaseData.builder()
            .allocationDecision(createAllocationDecision("wrong proposal", "reason"))
            .allocationProposal(createAllocationDecision("proposal", "reason"))
            .build();

        Allocation expectedDecision = Allocation.builder()
            .judgeLevelRadio("No")
            .proposal("wrong proposal")
            .proposalReason("reason")
            .allocationProposalPresent("Yes")
            .build();

        assertThat(courtAllocationService.createDecision(caseData)).isEqualTo(expectedDecision);
    }

    private Allocation createAllocationDecision(String proposal, String reason) {
        return Allocation.builder()
            .proposal(proposal)
            .proposalReason(reason)
            .build();
    }
}
