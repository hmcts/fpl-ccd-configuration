package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Service
public class CourtLevelAllocationService {
    public Allocation createDecision(CaseData caseData) {
        Allocation.AllocationBuilder decisionBuilder =
            populateDecision(caseData.getAllocationDecision());
        decisionBuilder.allocationProposalPresent(
            YesNo.from(hasAllocationPresent(caseData.getAllocationProposal())).getValue());

        if (hasAllocationPresent(caseData.getAllocationProposal())) {
            if (hasAllocationPresent(caseData.getAllocationDecision())) {
                if (caseData.getAllocationProposal().getProposal()
                    .equals(caseData.getAllocationDecision().getProposal())) {
                    decisionBuilder.judgeLevelRadio(YES.getValue());
                } else {
                    decisionBuilder.judgeLevelRadio(NO.getValue());
                }
            } else {
                decisionBuilder.judgeLevelRadio(null);
            }
        }

        return decisionBuilder.build();
    }

    public Allocation setAllocationDecisionIfNull(CaseData caseData) {
        return setAllocationDecisionIfNull(caseData, caseData.getAllocationDecision());
    }

    public Allocation setAllocationDecisionIfNull(CaseData caseData, Allocation allocationDecision) {
        Allocation.AllocationBuilder decisionBuilder = populateDecision(allocationDecision);

        if (allocationDecision.getProposal() == null) {
            decisionBuilder.proposal(caseData.getAllocationProposal().getProposal());
            decisionBuilder.proposalReason(caseData.getAllocationProposal().getProposalReason());
        }

        // Setting radio here as to not display the question in tab
        decisionBuilder.judgeLevelRadio(null);

        return decisionBuilder.build();
    }

    private boolean hasAllocationPresent(Allocation data) {
        return data != null && isNotEmpty(data.getProposal());
    }

    private Allocation.AllocationBuilder populateDecision(Allocation allocationDecision) {
        return ofNullable(allocationDecision)
            .map(Allocation::toBuilder)
            .orElse(Allocation.builder());
    }
}
