package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Proceeding;
import uk.gov.hmcts.reform.fpl.model.tasklist.TaskState;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED_FINISHED;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.anyEmpty;

@Component
public class ProceedingsChecker implements EventChecker {

    @Override
    public List<String> validate(CaseData caseData) {
        return emptyList();
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        Proceeding proceedings = caseData.getProceeding();
        return isNotEmpty(proceedings) && isNotEmpty(proceedings.getOnGoingProceeding());
    }

    @Override
    public boolean isCompleted(CaseData caseData) {
        final Proceeding proceeding = caseData.getProceeding();

        if (proceeding == null || isEmpty(proceeding.getOnGoingProceeding())) {
            return false;
        }

        if (YES.getValue().equals(proceeding.getOnGoingProceeding())) {
            if (NO.getValue().equals(proceeding.getSameGuardianNeeded())
                && isEmpty(proceeding.getSameGuardianDetails())) {
                return false;
            }
            return !anyEmpty(
                proceeding.getProceedingStatus(),
                proceeding.getCaseNumber(),
                proceeding.getStarted(),
                proceeding.getEnded(),
                proceeding.getOrdersMade(),
                proceeding.getChildren(),
                proceeding.getGuardian()
            );
        }

        return true;
    }

    @Override
    public TaskState completedState() {
        return COMPLETED_FINISHED;
    }
}
