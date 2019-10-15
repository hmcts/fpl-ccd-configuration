package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Service
public class CourtLevelAllocationService {
    public Allocation createDecision(CaseData caseData) {
        Allocation.AllocationBuilder decisionBuilder =
            populateDecision(caseData.getAllocationDecision());
        decisionBuilder.allocationProposalPresent(checkIfAllocationIsPresent(caseData.getAllocationProposal()));

        if (checkIfAllocationIsPresent(caseData.getAllocationProposal()).equals("Yes")) {
            if (checkIfAllocationIsPresent(caseData.getAllocationDecision()).equals("Yes")) {
                if (caseData.getAllocationProposal().getProposal()
                    .equals(caseData.getAllocationDecision().getProposal())) {
                    decisionBuilder.judgeLevelRadio("Yes");
                } else {
                    decisionBuilder.judgeLevelRadio("No");
                }
            } else {
                decisionBuilder.judgeLevelRadio(null);
            }
        }

        return decisionBuilder.build();
    }

    private Allocation.AllocationBuilder populateDecision(Allocation allocationDecision) {
        return ofNullable(allocationDecision)
            .map(Allocation::toBuilder)
            .orElse(Allocation.builder());
    }

    private String checkIfAllocationIsPresent(Allocation data) {
        return data != null && isNotEmpty(data.getProposal()) ? "Yes" : "No";
    }

    public Allocation setAllocationDecisionIfNull(CaseData caseData) {
        Allocation.AllocationBuilder decisionBuilder =
            populateDecision(caseData.getAllocationDecision());

        if (caseData.getAllocationDecision().getProposal() == null) {
            decisionBuilder.proposal(caseData.getAllocationProposal().getProposal());
            decisionBuilder.judgeLevelRadio(null);
        } else {
            // Setting radio here as to not display the question in tab
            decisionBuilder.judgeLevelRadio(null);
        }

        return decisionBuilder.build();
    }
}
