package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.Task;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.Task.task;
import static uk.gov.hmcts.reform.fpl.TaskState.COMPLETED;
import static uk.gov.hmcts.reform.fpl.TaskState.IN_PROGRESS;
import static uk.gov.hmcts.reform.fpl.TaskState.NOT_AVAILABLE;
import static uk.gov.hmcts.reform.fpl.enums.Event.ALLOCATION_PROPOSAL;
import static uk.gov.hmcts.reform.fpl.enums.Event.APPLICANT;
import static uk.gov.hmcts.reform.fpl.enums.Event.ATTENDING_THE_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.fpl.enums.Event.DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.ENTER_CHILDREN;
import static uk.gov.hmcts.reform.fpl.enums.Event.ENTER_OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.Event.FACTORS_AFFECTING_PARENTING;
import static uk.gov.hmcts.reform.fpl.enums.Event.GROUNDS;
import static uk.gov.hmcts.reform.fpl.enums.Event.HEARING_NEEDED;
import static uk.gov.hmcts.reform.fpl.enums.Event.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.fpl.enums.Event.ORDERS_NEEDED;
import static uk.gov.hmcts.reform.fpl.enums.Event.OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.enums.Event.RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.RISK_AND_HARM;
import static uk.gov.hmcts.reform.fpl.enums.Event.SUBMIT_APPLICATION;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readString;

@ExtendWith(SpringExtension.class)
class TaskListRendererTest {

    private static final String IMAGES_BASE_URL
            = "https://raw.githubusercontent.com/hmcts/fpl-ccd-configuration/master/resources/";

    private TaskListRenderer taskListRenderer = new TaskListRenderer(IMAGES_BASE_URL);

    @Test
    void shouldRenderTaskList() {
        final List<Task> tasks = List.of(
                task(CASE_NAME, COMPLETED),
                task(ORDERS_NEEDED, IN_PROGRESS),
                task(HEARING_NEEDED, COMPLETED),
                task(GROUNDS, COMPLETED),
                task(RISK_AND_HARM, IN_PROGRESS),
                task(FACTORS_AFFECTING_PARENTING, COMPLETED),
                task(DOCUMENTS, COMPLETED),
                task(APPLICANT, COMPLETED),
                task(ENTER_CHILDREN, COMPLETED),
                task(RESPONDENTS, IN_PROGRESS),
                task(ALLOCATION_PROPOSAL, COMPLETED),
                task(OTHER_PROCEEDINGS, IN_PROGRESS),
                task(INTERNATIONAL_ELEMENT, IN_PROGRESS),
                task(ENTER_OTHERS, IN_PROGRESS),
                task(ATTENDING_THE_HEARING, IN_PROGRESS),
                task(SUBMIT_APPLICATION, NOT_AVAILABLE));

        final String actualTaskList = taskListRenderer.render(tasks);
        final String expectedTaskList = readString("task-list/expected-task-list.md").trim();

        assertThat(actualTaskList).isEqualTo(expectedTaskList);
    }
}
