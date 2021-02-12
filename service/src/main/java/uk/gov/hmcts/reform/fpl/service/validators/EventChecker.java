package uk.gov.hmcts.reform.fpl.service.validators;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.tasklist.TaskState;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED;

public interface EventChecker {

    List<String> validate(CaseData caseData);

    boolean isStarted(CaseData caseData);

    default boolean isCompleted(CaseData caseData) {
        return validate(caseData).isEmpty();
    }

    default boolean isAvailable(CaseData caseData) {
        return true;
    }

    default TaskState completedState() {
        return COMPLETED;
    }
}
