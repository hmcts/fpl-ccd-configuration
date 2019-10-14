package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.AllocationDecision;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Service

//TODO: CourtLevel in name would be nice
public class CourtAllocationService {

    public AllocationDecision createDecision(CaseData caseData) {
        AllocationDecision.AllocationDecisionBuilder decisionBuilder =
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

    //TODO: some of this logic is used in aboutToSubmit
    private AllocationDecision.AllocationDecisionBuilder populateDecision(AllocationDecision allocationDecision) {
        return ofNullable(allocationDecision)
            .map(AllocationDecision::toBuilder)
            .orElse(AllocationDecision.builder());
    }

    //TODO: defaultIfNull. Could we
    private String checkIfAllocationIsPresent(AllocationDecision data) {
        return data != null && isNotEmpty(data.getProposal()) ? "Yes" : "No";
    }
}
