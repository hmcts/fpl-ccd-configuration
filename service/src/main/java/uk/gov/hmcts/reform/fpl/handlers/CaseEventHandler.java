package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.CaseDataChanged;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.tasklist.Task;
import uk.gov.hmcts.reform.fpl.service.TaskListRenderer;
import uk.gov.hmcts.reform.fpl.service.TaskListService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.isInOpenState;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseEventHandler {

    private final ObjectMapper mapper;
    private final CoreCaseDataService coreCaseDataService;
    private final TaskListService taskListService;
    private final TaskListRenderer taskListRenderer;

    @EventListener
    public void handleCaseDataChange(final CaseDataChanged event) {
        final CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();

        if (isInOpenState(caseDetails)) {
            final CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
            final List<Task> tasks = taskListService.getTasksForOpenCase(caseData);
            final String taskList = taskListRenderer.render(tasks);

            coreCaseDataService.triggerEvent(
                    JURISDICTION,
                    CASE_TYPE,
                    caseDetails.getId(),
                    "internal-update-task-list",
                    Map.of("taskList", taskList));
        }
    }
}
