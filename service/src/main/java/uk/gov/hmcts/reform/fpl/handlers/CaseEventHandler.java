package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.CaseDataChanged;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.tasklist.Task;
import uk.gov.hmcts.reform.fpl.service.PreSubmissionTasksRenderer;
import uk.gov.hmcts.reform.fpl.service.PreSubmissionTasksService;
import uk.gov.hmcts.reform.fpl.service.TaskListRenderer;
import uk.gov.hmcts.reform.fpl.service.TaskListService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseEventHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final TaskListService taskListService;
    private final TaskListRenderer taskListRenderer;
    private final PreSubmissionTasksRenderer preSubmissionTasksRenderer;
    private final PreSubmissionTasksService preSubmissionTasksService;

    @EventListener
    public void handleCaseDataChange(final CaseDataChanged event) {
        final CaseData caseData = event.getCaseData();

        if (caseData.getState() == OPEN) {

            final List<Task> tasks = taskListService.getTasksForOpenCase(caseData);
            final String taskList = taskListRenderer.render(tasks) + getSubmissionTasks(caseData);

            coreCaseDataService.triggerEvent(
                JURISDICTION,
                CASE_TYPE,
                caseData.getId(),
                "internal-update-task-list",
                Map.of("taskList", taskList));
        }
    }

    private String getSubmissionTasks(CaseData caseData) {
        return preSubmissionTasksRenderer
            .render(preSubmissionTasksService.getEventValidationsForSubmission(caseData));
    }
}
