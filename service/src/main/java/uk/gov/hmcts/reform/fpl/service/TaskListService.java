package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.Task;
import uk.gov.hmcts.reform.fpl.TaskState;
import uk.gov.hmcts.reform.fpl.enums.Event;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.validators.EventChecker;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.TaskState.COMPLETED;
import static uk.gov.hmcts.reform.fpl.TaskState.IN_PROGRESS;
import static uk.gov.hmcts.reform.fpl.TaskState.NOT_AVAILABLE;
import static uk.gov.hmcts.reform.fpl.enums.Event.eventsInState;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TaskListService {

    private final EventChecker eventChecker;

    public List<Task> getTasksForOpenCase(CaseData caseData) {
        return eventsInState(OPEN).stream()
            .map(event -> Task.builder()
                .event(event)
                .state(getTaskState(caseData, event))
                .build())
            .collect(toList());
    }

    private TaskState getTaskState(CaseData caseData, Event event) {
        if (eventChecker.isCompleted(event, caseData)) {
            return COMPLETED;
        }

        if (!eventChecker.isAvailable(event, caseData)) {
            return NOT_AVAILABLE;
        }

        return IN_PROGRESS;
    }
}
