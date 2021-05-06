package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.gov.hmcts.reform.fpl.model.submission.EventValidationErrors;
import uk.gov.hmcts.reform.fpl.model.tasklist.Task;
import uk.gov.hmcts.reform.fpl.service.tasklist.TaskListRenderElements;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.Event.ALLOCATION_PROPOSAL;
import static uk.gov.hmcts.reform.fpl.enums.Event.APPLICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.fpl.enums.Event.CHILDREN;
import static uk.gov.hmcts.reform.fpl.enums.Event.COURT_SERVICES;
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
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED_FINISHED;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.IN_PROGRESS;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.NOT_AVAILABLE;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.NOT_STARTED;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readString;

class TaskListRendererTest {

    private final TaskListRenderer taskListRenderer = new TaskListRenderer(
        new TaskListRenderElements(
            "https://raw.githubusercontent.com/hmcts/fpl-ccd-configuration/master/resources/"
        ));

    private static List<Task> TASKS = List.of(
        task(CASE_NAME, COMPLETED_FINISHED),
        task(ORDERS_SOUGHT, IN_PROGRESS),
        task(HEARING_URGENCY, COMPLETED_FINISHED),
        task(GROUNDS, COMPLETED),
        task(RISK_AND_HARM, IN_PROGRESS),
        task(FACTORS_AFFECTING_PARENTING, COMPLETED_FINISHED),
        task(APPLICATION_DOCUMENTS, COMPLETED),
        task(ORGANISATION_DETAILS, COMPLETED),
        task(CHILDREN, COMPLETED),
        task(RESPONDENTS, IN_PROGRESS),
        task(ALLOCATION_PROPOSAL, COMPLETED),
        task(OTHER_PROCEEDINGS, NOT_STARTED),
        task(INTERNATIONAL_ELEMENT, IN_PROGRESS),
        task(OTHERS, NOT_STARTED),
        task(COURT_SERVICES, IN_PROGRESS),
        task(SUBMIT_APPLICATION, NOT_AVAILABLE));

    @Test
    void shouldRenderTaskListWithApplicationDocuments() {

        List<EventValidationErrors> eventErrors = List.of(
            EventValidationErrors.builder()
                .event(ORDERS_SOUGHT)
                .errors(List.of("Add the orders and directions sought"))
                .build());

        assertThat(taskListRenderer.render(TASKS, eventErrors))
            .isEqualTo(read("task-list/expected-task-list.md"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldRenderTaskListWithoutErrors(List<EventValidationErrors> errors) {
        assertThat(taskListRenderer.render(TASKS, errors))
            .isEqualTo(read("task-list/expected-task-list-no-errors.md"));
    }

    private static String read(String filename) {
        return readString(filename).trim();
    }
}
