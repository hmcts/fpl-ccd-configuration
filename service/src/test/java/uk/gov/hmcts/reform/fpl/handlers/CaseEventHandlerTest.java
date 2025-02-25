package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.CaseDataChanged;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.submission.EventValidationErrors;
import uk.gov.hmcts.reform.fpl.model.tasklist.Task;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.TaskListRenderer;
import uk.gov.hmcts.reform.fpl.service.TaskListService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.validators.CaseSubmissionChecker;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.fpl.enums.Event.SUBMIT_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.NOT_AVAILABLE;
import static uk.gov.hmcts.reform.fpl.service.CaseConverter.MAP_TYPE;

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

    @Mock
    private CaseConverter caseConverter;

    @InjectMocks
    private CaseEventHandler caseEventHandler;

    @Test
    void shouldTriggerCaseDataChange() {
        final long caseId = nextLong();
        final CaseData caseData = CaseData.builder()
            .id(caseId)
            .state(OPEN)
            .build();

        final List<Task> tasks = List.of(
            Task.builder().event(CASE_NAME).state(COMPLETED).build(),
            Task.builder().event(SUBMIT_APPLICATION).state(NOT_AVAILABLE).build());

        final List<EventValidationErrors> eventsErrors = List.of(
            EventValidationErrors.builder()
                .event(CASE_NAME)
                .errors(List.of("Change case name"))
                .build());

        final String renderedTaskLists = "<h1>Task 1</h1><h2>Task 2</h2>";

        ObjectMapper mapper = new ObjectMapper();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(mapper.convertValue(caseData, MAP_TYPE))
            .build();

        when(caseConverter.convert(caseDetails)).thenReturn(caseData);
        when(caseSubmissionChecker.validateAsGroups(caseData)).thenReturn(eventsErrors);
        when(taskListService.getTasksForOpenCase(caseData)).thenReturn(tasks);
        when(taskListRenderer.renderTasks(eq(tasks), eq(eventsErrors), eq(Optional.empty()),
            eq(Optional.of(Map.of())), eq(caseId), anyBoolean()))
            .thenReturn(renderedTaskLists);

        caseEventHandler.getUpdates(caseDetails);

        verify(taskListService).getTasksForOpenCase(caseData);
        verify(caseSubmissionChecker).validateAsGroups(caseData);
        verify(taskListRenderer, times(2)).renderTasks(eq(tasks), eq(eventsErrors), eq(Optional.empty()),
            eq(Optional.of(Map.of())), eq(caseId), anyBoolean());

    }

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


        final List<EventValidationErrors> eventsErrors = List.of(
            EventValidationErrors.builder()
                .event(CASE_NAME)
                .errors(List.of("Change case name"))
                .build());

        caseEventHandler.handleCaseDataChange(caseDataChanged);

        verify(coreCaseDataService).performPostSubmitCallback(eq(caseData.getId()),
            eq("internal-update-task-list"), any());
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
