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
                if (caseData.getAllocationProposal().getProposalV2()
                    .equals(caseData.getAllocationDecision().getProposalV2())) {
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

    public Allocation createAllocationDecisionIfNull(CaseData caseData) {
        return createAllocationDecisionIfNull(caseData, caseData.getAllocationDecision());
    }

    public Allocation createAllocationDecisionIfNull(CaseData caseData, Allocation allocationDecision) {
        Allocation.AllocationBuilder decisionBuilder = populateDecision(allocationDecision);

        if (allocationDecision.getProposalV2() == null) {
            decisionBuilder.proposalV2(caseData.getAllocationProposal().getProposalV2());
            decisionBuilder.proposalReason(caseData.getAllocationProposal().getProposalReason());
        }

        // Setting radio here as to not display the question in tab
        decisionBuilder.judgeLevelRadio(null);

        return decisionBuilder.build();
    }

    private boolean hasAllocationPresent(Allocation data) {
        return data != null && isNotEmpty(data.getProposalV2());
    }

    private Allocation.AllocationBuilder populateDecision(Allocation allocationDecision) {
        return ofNullable(allocationDecision)
            .map(Allocation::toBuilder)
            .orElse(Allocation.builder());
    }
}
