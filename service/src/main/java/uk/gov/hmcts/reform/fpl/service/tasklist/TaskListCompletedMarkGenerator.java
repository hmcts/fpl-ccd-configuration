package uk.gov.hmcts.reform.fpl.service.tasklist;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.Event;
import uk.gov.hmcts.reform.fpl.model.tasklist.Task;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;

import java.util.Arrays;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.Event.ALLOCATION_PROPOSAL;
import static uk.gov.hmcts.reform.fpl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.fpl.enums.Event.COURT_SERVICES;
import static uk.gov.hmcts.reform.fpl.enums.Event.FACTORS_AFFECTING_PARENTING;
import static uk.gov.hmcts.reform.fpl.enums.Event.GROUNDS;
import static uk.gov.hmcts.reform.fpl.enums.Event.HEARING_URGENCY;
import static uk.gov.hmcts.reform.fpl.enums.Event.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.fpl.enums.Event.ORDERS_SOUGHT;
import static uk.gov.hmcts.reform.fpl.enums.Event.OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.enums.Event.RISK_AND_HARM;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TaskListCompletedMarkGenerator {

    private static final List<Event> EVENTS_TO_BE_MARKED_FINISHED = Arrays.asList(
        CASE_NAME,
        ORDERS_SOUGHT,
        HEARING_URGENCY,
        GROUNDS,
        RISK_AND_HARM,
        FACTORS_AFFECTING_PARENTING,
        ALLOCATION_PROPOSAL,
        OTHER_PROCEEDINGS,
        INTERNATIONAL_ELEMENT,
        COURT_SERVICES
    );

    private final FeatureToggleService featureToggleService;
    private final TaskListRenderElements taskListRenderElements;

    public String generate(Task task) {
        if (useFinishedTag(task)) {
            return taskListRenderElements.renderLink(task) + taskListRenderElements.renderImage("finished.png",
                "Finished");
        }

        return taskListRenderElements.renderLink(task) + taskListRenderElements.renderImage("information-added.png",
            "Information added");
    }

    private boolean useFinishedTag(Task task) {
        if (!featureToggleService.isFinishedTagEnabled()) {
            return false;
        }
        return EVENTS_TO_BE_MARKED_FINISHED.contains(task.getEvent());
    }

}
