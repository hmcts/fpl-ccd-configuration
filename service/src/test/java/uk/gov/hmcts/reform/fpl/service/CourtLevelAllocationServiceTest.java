package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;

class CourtLevelAllocationServiceTest {

    private final CourtLevelAllocationService courtAllocationService = new CourtLevelAllocationService();

    @Test
    void shouldAddYesWhenAllocationDecisionPresent() {
        CaseData caseData = CaseData.builder()
            .allocationDecision(createAllocation("test", "decision reason"))
            .allocationProposal(createAllocation("proposal", "reason"))
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
    void shouldAddNoWhenAllocationDecisionNotPresent() {
        CaseData caseData = CaseData.builder()
            .build();

        Allocation expectedDecision = Allocation.builder()
            .allocationProposalPresent("No")
            .build();

        assertThat(courtAllocationService.createDecision(caseData)).isEqualTo(expectedDecision);
    }

    @Test
    void shouldAddNullToJudgeLevelRadioWhenAllocationDecisionHasNotBeenMade() {
        CaseData caseData = CaseData.builder()
            .allocationProposal(createAllocation("proposal", "reason"))
            .build();

        Allocation expectedDecision = Allocation.builder()
            .judgeLevelRadio(null)
            .allocationProposalPresent("Yes")
            .build();

        assertThat(courtAllocationService.createDecision(caseData)).isEqualTo(expectedDecision);
    }

    @Test
    void shouldAddYesToJudgeLevelRadioWhenAllocationProposalIsCorrect() {
        CaseData caseData = CaseData.builder()
            .allocationDecision(createAllocation("proposal", "reason"))
            .allocationProposal(createAllocation("proposal", "reason"))
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
    void shouldAddNoToJudgeLevelRadioWhenAllocationProposalIsIncorrect() {
        CaseData caseData = CaseData.builder()
            .allocationDecision(createAllocation("wrong proposal", "reason"))
            .allocationProposal(createAllocation("proposal", "reason"))
            .build();

        Allocation expectedDecision = Allocation.builder()
            .judgeLevelRadio("No")
            .proposal("wrong proposal")
            .proposalReason("reason")
            .allocationProposalPresent("Yes")
            .build();

        assertThat(courtAllocationService.createDecision(caseData)).isEqualTo(expectedDecision);
    }

    @Test
    void shouldSetAllocationDecisionEqualToAllocationProposalWhenNull() {
        CaseData caseData = CaseData.builder()
            .allocationProposal(createAllocation("proposal", "reason"))
            .allocationDecision(createAllocation(null, null))
            .build();

        Allocation expectedDecision = Allocation.builder()
            .judgeLevelRadio(null)
            .proposal("proposal")
            .proposalReason("reason")
            .allocationProposalPresent(null)
            .judgeLevelRadio(null)
            .build();

        Allocation allocationDecision = courtAllocationService.setAllocationDecisionIfNull(caseData);

        assertThat(allocationDecision.equals(expectedDecision));
    }

    private Allocation createAllocation(String proposal, String reason) {
        return Allocation.builder()
            .proposal(proposal)
            .proposalReason(reason)
            .build();
    }
}
