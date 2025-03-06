package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Proceeding;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.tasklist.TaskState;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.ProceedingStatus.PREVIOUS;
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
        return isNotEmpty(caseData.getProceedings());
    }

    @Override
    public boolean isCompleted(CaseData caseData) {
        final List<Element<Proceeding>> proceedings = caseData.getProceedings();
        if (isEmpty(proceedings)) {
            return false;
        }

        for (Element<Proceeding> proceedingElement: caseData.getProceedings()) {
            final Proceeding proceeding = proceedingElement.getValue();

            if (anyEmpty(
                proceeding.getProceedingStatus(),
                proceeding.getCaseNumber(),
                proceeding.getStarted(),
                proceeding.getOrdersMade(),
                proceeding.getChildren(),
                proceeding.getGuardian(),
                proceeding.getSameGuardianNeeded())) {
                return false;
            }

            if (PREVIOUS.equals(proceeding.getProceedingStatus()) && isEmpty(proceeding.getEnded())) {
                return false;
            }

            if (NO.equals(proceeding.getSameGuardianNeeded()) && isEmpty(proceeding.getSameGuardianDetails())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public TaskState completedState() {
        return COMPLETED_FINISHED;
    }
}
