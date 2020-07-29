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
import uk.gov.hmcts.reform.fpl.service.validators.EventChecker;

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
import static uk.gov.hmcts.reform.fpl.enums.Event.APPLICANT;
import static uk.gov.hmcts.reform.fpl.enums.Event.ATTENDING_THE_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.fpl.enums.Event.CHILDREN;
import static uk.gov.hmcts.reform.fpl.enums.Event.DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.FACTORS_AFFECTING_PARENTING;
import static uk.gov.hmcts.reform.fpl.enums.Event.GROUNDS;
import static uk.gov.hmcts.reform.fpl.enums.Event.HEARING_NEEDED;
import static uk.gov.hmcts.reform.fpl.enums.Event.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.fpl.enums.Event.ORDERS_NEEDED;
import static uk.gov.hmcts.reform.fpl.enums.Event.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.Event.OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.enums.Event.RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.RISK_AND_HARM;
import static uk.gov.hmcts.reform.fpl.enums.Event.SUBMIT_APPLICATION;
import static uk.gov.hmcts.reform.fpl.model.tasklist.Task.task;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.IN_PROGRESS;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.NOT_AVAILABLE;


@ExtendWith(SpringExtension.class)
class TaskListServiceTest {

    @Mock
    private EventChecker eventChecker;

    @InjectMocks
    private TaskListService taskListService;

    private CaseData caseData = CaseData.builder().build();

    @Test
    void shouldReturnTasksInProgress() {
        when(eventChecker.isCompleted(any(Event.class), eq(caseData))).thenReturn(false);
        when(eventChecker.isAvailable(any(Event.class), eq(caseData))).thenReturn(true);

        final List<Task> tasks = taskListService.getTasksForOpenCase(caseData);

        assertThat(tasks).containsExactlyInAnyOrderElementsOf(getTasks(IN_PROGRESS));
    }

    @Test
    void shouldReturnCompletedTasks() {
        when(eventChecker.isCompleted(any(Event.class), eq(caseData))).thenReturn(true);

        final List<Task> tasks = taskListService.getTasksForOpenCase(caseData);

        assertThat(tasks).containsExactlyInAnyOrderElementsOf(getTasks(COMPLETED));

        verify(eventChecker, never()).isAvailable(any(), any());
    }

    @Test
    void shouldReturnNotAvailableTasks() {
        when(eventChecker.isCompleted(any(Event.class), eq(caseData))).thenReturn(false);
        when(eventChecker.isAvailable(any(Event.class), eq(caseData))).thenReturn(false);

        final List<Task> tasks = taskListService.getTasksForOpenCase(caseData);

        assertThat(tasks).containsExactlyInAnyOrderElementsOf(getTasks(NOT_AVAILABLE));
    }

    private List<Task> getTasks(TaskState state) {
        return Stream.of(
                ORDERS_NEEDED,
                HEARING_NEEDED,
                GROUNDS,
                RISK_AND_HARM,
                FACTORS_AFFECTING_PARENTING,
                APPLICANT,
                CHILDREN,
                RESPONDENTS,
                ALLOCATION_PROPOSAL,
                OTHER_PROCEEDINGS,
                INTERNATIONAL_ELEMENT,
                OTHERS,
                ATTENDING_THE_HEARING,
                DOCUMENTS,
                CASE_NAME,
                SUBMIT_APPLICATION)
                .map(event -> task(event, state))
                .collect(Collectors.toList());
    }
}
