package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.CaseDataChanged;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.tasklist.Task;
import uk.gov.hmcts.reform.fpl.service.TaskListRenderer;
import uk.gov.hmcts.reform.fpl.service.TaskListService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
    private ObjectMapper objectMapper;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private TaskListService taskListService;

    @Mock
    private TaskListRenderer taskListRenderer;

    @InjectMocks
    private CaseEventHandler caseEventHandler;

    @Test
    void shouldUpdateTaskListForCasesInOpenState() {
        final CaseDetails caseDetails = CaseDetails.builder()
                .id(nextLong())
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .state(OPEN.getValue())
                .build();
        final CaseData caseData = CaseData.builder().build();
        final CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        final CaseDataChanged caseDataChanged = new CaseDataChanged(callbackRequest);
        final List<Task> tasks = List.of(
                Task.builder().event(CASE_NAME).state(COMPLETED).build(),
                Task.builder().event(SUBMIT_APPLICATION).state(NOT_AVAILABLE).build()
        );
        final String renderedTaskLists = "<h1>Task 1</h1><h2>Task 2</h2>";

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(taskListService.getTasksForOpenCase(caseData)).thenReturn(tasks);
        when(taskListRenderer.render(tasks)).thenReturn(renderedTaskLists);

        caseEventHandler.handleCaseDataChange(caseDataChanged);

        verify(taskListService).getTasksForOpenCase(caseData);
        verify(taskListRenderer).render(tasks);
        verify(coreCaseDataService).triggerEvent(
                JURISDICTION,
                CASE_TYPE,
                caseDetails.getId(),
                "internal-update-task-list",
                Map.of("taskList", renderedTaskLists)
        );
    }

    @Test
    void shouldNotUpdateTaskListForCasesInStateDifferentThanOpen() {
        final CaseDetails caseDetails = CaseDetails.builder()
                .id(nextLong())
                .jurisdiction(JURISDICTION)
                .caseTypeId(CASE_TYPE)
                .state(SUBMITTED.getValue())
                .build();
        final CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        final CaseDataChanged caseDataChanged = new CaseDataChanged(callbackRequest);

        caseEventHandler.handleCaseDataChange(caseDataChanged);

        verify(taskListService, never()).getTasksForOpenCase(any());
        verify(taskListRenderer, never()).render(any());
        verify(coreCaseDataService, never()).triggerEvent(any(), any(), any(), any(), any());
    }
}
