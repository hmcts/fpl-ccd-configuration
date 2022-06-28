package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.events.CaseDataChanged;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.submission.EventValidationErrors;
import uk.gov.hmcts.reform.fpl.model.tasklist.Task;
import uk.gov.hmcts.reform.fpl.service.TaskListRenderer;
import uk.gov.hmcts.reform.fpl.service.TaskListService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.validators.CaseSubmissionChecker;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.fpl.enums.Event.SUBMIT_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.NOT_AVAILABLE;

@ExtendWith(MockitoExtension.class)
class CaseEventHandlerTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private TaskListService taskListService;

    @Mock
    private TaskListRenderer taskListRenderer;

    @Mock
    private CaseSubmissionChecker caseSubmissionChecker;

    @InjectMocks
    private CaseEventHandler caseEventHandler;

    @Test
    void shouldUpdateTaskListForCasesInOpenState() {
        final CaseData caseData = CaseData.builder()
            .id(nextLong())
            .state(OPEN)
            .build();
        final CaseDataChanged caseDataChanged = new CaseDataChanged(caseData);
        final List<Task> tasks = List.of(
            Task.builder().event(CASE_NAME).state(COMPLETED).build(),
            Task.builder().event(SUBMIT_APPLICATION).state(NOT_AVAILABLE).build());

        final String renderedTaskLists = "<h1>Task 1</h1><h2>Task 2</h2>";

        final List<EventValidationErrors> eventsErrors = List.of(
            EventValidationErrors.builder()
                .event(CASE_NAME)
                .errors(List.of("Change case name"))
                .build());

        when(caseSubmissionChecker.validateAsGroups(caseData)).thenReturn(eventsErrors);
        when(taskListService.getTasksForOpenCase(caseData)).thenReturn(tasks);
        when(taskListRenderer.render(tasks, eventsErrors)).thenReturn(renderedTaskLists);

        caseEventHandler.handleCaseDataChange(caseDataChanged);

        verify(taskListService).getTasksForOpenCase(caseData);
        verify(caseSubmissionChecker).validateAsGroups(caseData);
        verify(taskListRenderer).render(tasks, eventsErrors);

        verify(coreCaseDataService).triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            caseData.getId(),
            "internal-update-task-list",
            Map.of("taskList", renderedTaskLists)
        );
    }

    @Test
    void shouldNotUpdateTaskListForCasesInStateDifferentThanOpen() {
        final CaseData caseData = CaseData.builder()
            .id(nextLong())
            .state(SUBMITTED)
            .build();

        final CaseDataChanged caseDataChanged = new CaseDataChanged(caseData);

        caseEventHandler.handleCaseDataChange(caseDataChanged);

        verifyNoInteractions(taskListService, taskListRenderer, coreCaseDataService);
    }
}
