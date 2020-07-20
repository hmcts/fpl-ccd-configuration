package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.EventState;
import uk.gov.hmcts.reform.fpl.FplEvent;
import uk.gov.hmcts.reform.fpl.TaskListService;
import uk.gov.hmcts.reform.fpl.events.CaseDataChanged;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.FplEvent.eventsInState;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.isInOpenState;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseEventHandler {

    private final ObjectMapper mapper;
    private final CoreCaseDataService coreCaseDataService;
    private final TaskListService taskListService;
    private final TaskListRenderer taskListRenderer;

    @EventListener
    public void onNewEvent(final CaseDataChanged event) {
        final CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();

        if (isInOpenState(caseDetails)) {
            final CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
            final Map<FplEvent, EventState> tasks = taskListService.calculateState(caseData, eventsInState(OPEN));
            final String taskList = taskListRenderer.render(tasks);

            coreCaseDataService.triggerEvent(
                JURISDICTION,
                CASE_TYPE,
                caseDetails.getId(),
                "internal-update-case-info",
                Map.of("taskList", taskList));
        }
    }
}
