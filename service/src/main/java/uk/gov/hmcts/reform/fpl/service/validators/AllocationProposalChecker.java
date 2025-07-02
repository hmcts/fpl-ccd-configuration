package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.tasklist.TaskState;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED_FINISHED;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.allNonEmpty;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.anyNonEmpty;

@Component
public class AllocationProposalChecker extends PropertiesChecker {

    @Override
    public List<String> validate(CaseData caseData) {
        return super.validate(caseData, List.of("allocationProposal"));
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        final Allocation allocationProposal = caseData.getAllocationProposal();
        return isNotEmpty(allocationProposal)
            && anyNonEmpty(allocationProposal.getProposalV2(), allocationProposal.getProposalReason());
    }

    @Override
    public boolean isCompleted(CaseData caseData) {
        final Allocation allocationProposal = caseData.getAllocationProposal();

        return allocationProposal != null
            && allNonEmpty(
                allocationProposal.getProposalV2(),
                allocationProposal.getProposalReason()
            );
    }

    @Override
    public TaskState completedState() {
        return COMPLETED_FINISHED;
    }
}
