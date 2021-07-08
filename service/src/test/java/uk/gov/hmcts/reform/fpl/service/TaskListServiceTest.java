package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.Event;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.tasklist.Task;
import uk.gov.hmcts.reform.fpl.model.tasklist.TaskState;
import uk.gov.hmcts.reform.fpl.service.validators.EventsChecker;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.Event.ALLOCATION_PROPOSAL;
import static uk.gov.hmcts.reform.fpl.enums.Event.APPLICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.fpl.enums.Event.CHILDREN;
import static uk.gov.hmcts.reform.fpl.enums.Event.COURT_SERVICES;
import static uk.gov.hmcts.reform.fpl.enums.Event.DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.FACTORS_AFFECTING_PARENTING;
import static uk.gov.hmcts.reform.fpl.enums.Event.GROUNDS;
import static uk.gov.hmcts.reform.fpl.enums.Event.HEARING_URGENCY;
import static uk.gov.hmcts.reform.fpl.enums.Event.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.fpl.enums.Event.LANGUAGE_REQUIREMENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.ORDERS_SOUGHT;
import static uk.gov.hmcts.reform.fpl.enums.Event.ORGANISATION_DETAILS;
import static uk.gov.hmcts.reform.fpl.enums.Event.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.Event.OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.enums.Event.RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.RISK_AND_HARM;
import static uk.gov.hmcts.reform.fpl.enums.Event.SUBMIT_APPLICATION;
import static uk.gov.hmcts.reform.fpl.model.tasklist.Task.task;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.IN_PROGRESS;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.NOT_AVAILABLE;


@ExtendWith(SpringExtension.class)
class TaskListServiceTest {

    private static final TaskState COMPLETED_TASK_STATE = TaskState.COMPLETED_FINISHED;

    @Mock
    private EventsChecker eventsChecker;

    @InjectMocks
    private TaskListService taskListService;

    private CaseData caseData = CaseData.builder().build();

    @Test
    void shouldReturnTasksInProgress() {
        when(eventsChecker.isInProgress(any(Event.class), eq(caseData))).thenReturn(true);
        when(eventsChecker.isCompleted(any(Event.class), eq(caseData))).thenReturn(false);

        final List<Task> tasks = taskListService.getTasksForOpenCase(caseData);

        assertThat(tasks).containsExactlyInAnyOrderElementsOf(getTasks(IN_PROGRESS));

        verify(eventsChecker, never()).isAvailable(any(), any());
    }

    @Test
    void shouldReturnCompletedTasks() {
        when(eventsChecker.isCompleted(any(Event.class), eq(caseData))).thenReturn(true);
        when(eventsChecker.completedState(any(Event.class))).thenReturn(COMPLETED_TASK_STATE);

        final List<Task> tasks = taskListService.getTasksForOpenCase(caseData);

        assertThat(tasks).containsExactlyInAnyOrderElementsOf(getTasks(COMPLETED_TASK_STATE));

        verify(eventsChecker, never()).isAvailable(any(), any());
        verify(eventsChecker, never()).isInProgress(any(), any());
    }

    @Test
    void shouldReturnNotAvailableTasks() {
        when(eventsChecker.isCompleted(any(Event.class), eq(caseData))).thenReturn(false);
        when(eventsChecker.isAvailable(any(Event.class), eq(caseData))).thenReturn(false);

        final List<Task> tasks = taskListService.getTasksForOpenCase(caseData);

        verify(eventsChecker, never()).completedState(any(Event.class));
        assertThat(tasks).containsExactlyInAnyOrderElementsOf(getTasks(NOT_AVAILABLE));
    }

    private List<Task> getTasks(TaskState state) {
        return Stream.of(
            ORDERS_SOUGHT,
            HEARING_URGENCY,
            GROUNDS,
            RISK_AND_HARM,
            FACTORS_AFFECTING_PARENTING,
            ORGANISATION_DETAILS,
            CHILDREN,
            RESPONDENTS,
            ALLOCATION_PROPOSAL,
            OTHER_PROCEEDINGS,
            INTERNATIONAL_ELEMENT,
            OTHERS,
            COURT_SERVICES,
            DOCUMENTS,
            CASE_NAME,
            APPLICATION_DOCUMENTS,
            SUBMIT_APPLICATION,
            LANGUAGE_REQUIREMENTS)
            .map(event -> task(event, state))
            .collect(Collectors.toList());
    }
}
