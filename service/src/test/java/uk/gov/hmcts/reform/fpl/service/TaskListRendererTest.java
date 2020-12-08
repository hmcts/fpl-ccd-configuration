package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.Event;
import uk.gov.hmcts.reform.fpl.model.tasklist.Task;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.enums.Event.ALLOCATION_PROPOSAL;
import static uk.gov.hmcts.reform.fpl.enums.Event.APPLICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.fpl.enums.Event.CHILDREN;
import static uk.gov.hmcts.reform.fpl.enums.Event.COURT_SERVICES;
import static uk.gov.hmcts.reform.fpl.enums.Event.DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.FACTORS_AFFECTING_PARENTING;
import static uk.gov.hmcts.reform.fpl.enums.Event.GROUNDS;
import static uk.gov.hmcts.reform.fpl.enums.Event.HEARING_URGENCY;
import static uk.gov.hmcts.reform.fpl.enums.Event.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.fpl.enums.Event.ORDERS_SOUGHT;
import static uk.gov.hmcts.reform.fpl.enums.Event.ORGANISATION_DETAILS;
import static uk.gov.hmcts.reform.fpl.enums.Event.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.Event.OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.enums.Event.RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.RISK_AND_HARM;
import static uk.gov.hmcts.reform.fpl.enums.Event.SUBMIT_APPLICATION;
import static uk.gov.hmcts.reform.fpl.model.tasklist.Task.task;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.IN_PROGRESS;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.NOT_AVAILABLE;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.NOT_STARTED;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readString;


class TaskListRendererTest {
    private final FeatureToggleService featureToggleService = mock(FeatureToggleService.class);
    private final TaskListRenderer taskListRenderer = new TaskListRenderer(
        "https://raw.githubusercontent.com/hmcts/fpl-ccd-configuration/master/resources/",
        featureToggleService
    );

    private List<Task> getTasks(Event event) {
        return List.of(
            task(CASE_NAME, COMPLETED),
            task(ORDERS_SOUGHT, IN_PROGRESS),
            task(HEARING_URGENCY, COMPLETED),
            task(GROUNDS, COMPLETED),
            task(RISK_AND_HARM, IN_PROGRESS),
            task(FACTORS_AFFECTING_PARENTING, COMPLETED),
            task(event, COMPLETED),
            task(ORGANISATION_DETAILS, COMPLETED),
            task(CHILDREN, COMPLETED),
            task(RESPONDENTS, IN_PROGRESS),
            task(ALLOCATION_PROPOSAL, COMPLETED),
            task(OTHER_PROCEEDINGS, NOT_STARTED),
            task(INTERNATIONAL_ELEMENT, IN_PROGRESS),
            task(OTHERS, NOT_STARTED),
            task(COURT_SERVICES, IN_PROGRESS),
            task(SUBMIT_APPLICATION, NOT_AVAILABLE));
    }

    @Test
    void shouldRenderTaskListWhenApplicationDocumentsIsToggledOff() {
        given(featureToggleService.isApplicationDocumentsEventEnabled()).willReturn(false);

        final String expectedTaskList = readString("task-list/expected-task-list.md").trim();

        assertThat(taskListRenderer.render(getTasks(DOCUMENTS))).isEqualTo(expectedTaskList);
    }

    @Test
    void shouldRenderTaskListWhenApplicationDocumentsIsToggledOn() {
        given(featureToggleService.isApplicationDocumentsEventEnabled()).willReturn(true);

        final String expectedTaskList = readString("task-list/expected-task-list-when-application-documents-enabled.md")
            .trim();

        assertThat(taskListRenderer.render(getTasks(APPLICATION_DOCUMENTS))).isEqualTo(expectedTaskList);
    }
}
