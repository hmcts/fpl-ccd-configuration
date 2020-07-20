package uk.gov.hmcts.reform.fpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.controllers.guards.EventValidatorProvider;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;
import static uk.gov.hmcts.reform.fpl.EventState.COMPLETED;
import static uk.gov.hmcts.reform.fpl.EventState.NOT_AVAILABLE;
import static uk.gov.hmcts.reform.fpl.EventState.UNKNOWN;

@Service
public class TaskListService {

    @Autowired
    private EventValidatorProvider eventValidator;

    public Map<FplEvent, EventState> calculateState(CaseData caseData, List<FplEvent> events) {
        return events.stream()
            .collect(toMap(Function.identity(), event -> calculateState(caseData, event)));
    }

    private EventState calculateState(CaseData caseData, FplEvent event) {
        if (eventValidator.isCompleted(event, caseData)) {
            return COMPLETED;
        }

        if (!eventValidator.isAvailable(event, caseData)) {
            return NOT_AVAILABLE;
        }

        return UNKNOWN;
    }
}
