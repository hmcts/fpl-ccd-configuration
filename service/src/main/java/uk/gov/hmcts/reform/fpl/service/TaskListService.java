package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.FplEvent;
import uk.gov.hmcts.reform.fpl.Task;
import uk.gov.hmcts.reform.fpl.TaskState;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.validators.EventChecker;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.TaskState.COMPLETED;
import static uk.gov.hmcts.reform.fpl.TaskState.NOT_AVAILABLE;
import static uk.gov.hmcts.reform.fpl.TaskState.UNKNOWN;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TaskListService {

    private final EventChecker eventChecker;

    public List<Task> getTasks(CaseData caseData, List<FplEvent> events) {
        return events.stream()
            .map(event -> Task.builder()
                .event(event)
                .state(getTaskState(caseData, event))
                .build())
            .collect(toList());
    }

    private TaskState getTaskState(CaseData caseData, FplEvent event) {
        if (eventChecker.isCompleted(event, caseData)) {
            return COMPLETED;
        }

        if (!eventChecker.isAvailable(event, caseData)) {
            return NOT_AVAILABLE;
        }

        return UNKNOWN;
    }
}
