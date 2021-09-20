package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.tasklist.TaskState;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED_FINISHED;

@Service
public class CourtSelectionChecker implements EventChecker {

    @Override
    public List<String> validate(CaseData caseData) {
        if (YES.equals(caseData.getMultiCourts()) && isEmpty(caseData.getCourt())) {
            return List.of("Select court");
        }

        return emptyList();
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        return isNotEmpty(caseData.getCourt());
    }

    @Override
    public boolean isCompleted(CaseData caseData) {
        return isNotEmpty(caseData.getCourt());
    }

    @Override
    public TaskState completedState() {
        return COMPLETED_FINISHED;
    }
}
