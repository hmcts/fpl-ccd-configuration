package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.Event;
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

import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseEventHandler {

    private final CoreCaseDataService coreCaseDataService;
    private final TaskListService taskListService;
    private final TaskListRenderer taskListRenderer;
    private final CaseSubmissionChecker caseSubmissionChecker;
    private final CaseConverter caseConverter;

    public Map<String, Object> getUpdates(CaseDetails caseDetails) {
        CaseData caseData = caseConverter.convert(caseDetails);
        final List<Task> tasks = taskListService.getTasksForOpenCase(caseData);
        final List<EventValidationErrors> eventErrors = caseSubmissionChecker.validateAsGroups(caseData);
        final String taskList = taskListRenderer.renderTasks(tasks, eventErrors,
            getApplicationType(caseData),
            Optional.of(taskListService.getTaskHints(caseData, false)),
            caseDetails.getId(), false);

        final String taskListWelsh = taskListRenderer.renderTasks(tasks, eventErrors,
            getApplicationType(caseData),
            Optional.of(taskListService.getTaskHints(caseData, true)),
            caseDetails.getId(), true);

        return Map.of("taskList", taskList,
            "taskListWelsh", taskListWelsh);
    }

    @EventListener
    public void handleCaseDataChange(final CaseDataChanged event) {
        final CaseData caseData = event.getCaseData();

        if (caseData.getState() == OPEN) {
            coreCaseDataService.performPostSubmitCallback(
                caseData.getId(),
                "internal-update-task-list",
                this::getUpdates
            );
        }
    }

    public Optional<String> getApplicationType(CaseData caseData) {
        if (caseData.getOrders() != null) {
            return Optional.of(caseData.getOrders().isC1Order() ? "C1" : "C110a");
        }
        return Optional.empty();
    }
}
