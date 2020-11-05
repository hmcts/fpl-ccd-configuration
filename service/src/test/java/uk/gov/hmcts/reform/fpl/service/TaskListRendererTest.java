package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.tasklist.Task;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.Event.ALLOCATION_PROPOSAL;
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

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TaskListRenderer.class})
@TestPropertySource(properties = {
    "resources.images.baseUrl=https://raw.githubusercontent.com/hmcts/fpl-ccd-configuration/master/resources/"
})
class TaskListRendererTest {

    private static final List<Task> TASKS_TO_RENDER = List.of(
        task(CASE_NAME, COMPLETED),
        task(ORDERS_SOUGHT, IN_PROGRESS),
        task(HEARING_URGENCY, COMPLETED),
        task(GROUNDS, COMPLETED),
        task(RISK_AND_HARM, IN_PROGRESS),
        task(FACTORS_AFFECTING_PARENTING, COMPLETED),
        task(DOCUMENTS, COMPLETED),
        task(ORGANISATION_DETAILS, COMPLETED),
        task(CHILDREN, COMPLETED),
        task(RESPONDENTS, IN_PROGRESS),
        task(ALLOCATION_PROPOSAL, COMPLETED),
        task(OTHER_PROCEEDINGS, NOT_STARTED),
        task(INTERNATIONAL_ELEMENT, IN_PROGRESS),
        task(OTHERS, NOT_STARTED),
        task(COURT_SERVICES, IN_PROGRESS),
        task(SUBMIT_APPLICATION, NOT_AVAILABLE));

    @Autowired
    private TaskListRenderer taskListRenderer;

    @Test
    void shouldRenderTaskList() {
        final String expectedTaskList = readString("task-list/expected-task-list.md").trim();

        assertThat(taskListRenderer.render(TASKS_TO_RENDER)).isEqualTo(expectedTaskList);
    }
}
