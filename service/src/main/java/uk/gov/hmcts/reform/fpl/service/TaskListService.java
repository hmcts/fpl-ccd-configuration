package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.Event;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.tasklist.Task;
import uk.gov.hmcts.reform.fpl.model.tasklist.TaskState;
import uk.gov.hmcts.reform.fpl.service.validators.EventsChecker;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.enums.Event.eventsInState;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.IN_PROGRESS;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.NOT_AVAILABLE;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.NOT_STARTED;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TaskListService {

    private final EventsChecker eventsChecker;

    public List<Task> getTasksForOpenCase(CaseData caseData) {
        return eventsInState(OPEN).stream()
            .map(event -> Task.builder()
                .event(event)
                .state(getTaskState(caseData, event))
                .build())
            .collect(toList());
    }

    private TaskState getTaskState(CaseData caseData, Event event) {
        if (eventsChecker.isCompleted(event, caseData)) {
            return eventsChecker.completedState(event);
        }

        if (eventsChecker.isInProgress(event, caseData)) {
            return IN_PROGRESS;
        }

        if (!eventsChecker.isAvailable(event, caseData)) {
            return NOT_AVAILABLE;
        }

        return NOT_STARTED;
    }
}
