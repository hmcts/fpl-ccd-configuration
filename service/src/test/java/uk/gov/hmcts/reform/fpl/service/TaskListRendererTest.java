package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.gov.hmcts.reform.fpl.model.submission.EventValidationErrors;
import uk.gov.hmcts.reform.fpl.model.tasklist.Task;
import uk.gov.hmcts.reform.fpl.service.tasklist.TaskListRenderElements;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.Event.ALLOCATION_PROPOSAL;
import static uk.gov.hmcts.reform.fpl.enums.Event.APPLICANT_DETAILS_LA;
import static uk.gov.hmcts.reform.fpl.enums.Event.APPLICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.fpl.enums.Event.CHILDREN;
import static uk.gov.hmcts.reform.fpl.enums.Event.COURT_SERVICES;
import static uk.gov.hmcts.reform.fpl.enums.Event.FACTORS_AFFECTING_PARENTING;
import static uk.gov.hmcts.reform.fpl.enums.Event.GROUNDS;
import static uk.gov.hmcts.reform.fpl.enums.Event.HEARING_URGENCY;
import static uk.gov.hmcts.reform.fpl.enums.Event.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.fpl.enums.Event.LANGUAGE_REQUIREMENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.ORDERS_SOUGHT;
import static uk.gov.hmcts.reform.fpl.enums.Event.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.Event.OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.enums.Event.RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.RISK_AND_HARM;
import static uk.gov.hmcts.reform.fpl.enums.Event.SELECT_COURT;
import static uk.gov.hmcts.reform.fpl.enums.Event.SUBMIT_APPLICATION;
import static uk.gov.hmcts.reform.fpl.model.tasklist.Task.task;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED_FINISHED;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.IN_PROGRESS;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.NOT_AVAILABLE;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.NOT_STARTED;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readString;

class TaskListRendererTest {

    private final FeatureToggleService toggleService = mock(FeatureToggleService.class);

    private final TaskListRenderer taskListRenderer = new TaskListRenderer(
        new TaskListRenderElements(
            "https://raw.githubusercontent.com/hmcts/fpl-ccd-configuration/master/resources/"
        ), toggleService);

    @Nested
    class WithLocalAuthority {
        private final List<Task> tasks = List.of(
            task(CASE_NAME, COMPLETED_FINISHED),
            task(ORDERS_SOUGHT, IN_PROGRESS),
            task(HEARING_URGENCY, COMPLETED_FINISHED),
            task(GROUNDS, COMPLETED),
            task(RISK_AND_HARM, IN_PROGRESS),
            task(APPLICATION_DOCUMENTS, COMPLETED),
            task(APPLICANT_DETAILS_LA, COMPLETED),
            task(CHILDREN, COMPLETED),
            task(RESPONDENTS, IN_PROGRESS),
            task(ALLOCATION_PROPOSAL, COMPLETED),
            task(OTHER_PROCEEDINGS, NOT_STARTED),
            task(INTERNATIONAL_ELEMENT, IN_PROGRESS),
            task(OTHERS, NOT_STARTED),
            task(COURT_SERVICES, IN_PROGRESS),
            task(SUBMIT_APPLICATION, NOT_AVAILABLE),
            task(LANGUAGE_REQUIREMENTS, COMPLETED_FINISHED));

        @Test
        void shouldRenderTaskListWithApplicationDocuments() {
            when(toggleService.isLanguageRequirementsEnabled()).thenReturn(true);
            List<EventValidationErrors> eventErrors = List.of(
                EventValidationErrors.builder()
                    .event(ORDERS_SOUGHT)
                    .errors(List.of("Add the orders and directions sought"))
                    .build());

            assertThat(taskListRenderer.render(tasks, eventErrors))
                .isEqualTo(read("task-list/expected-task-list.md"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldRenderTaskListWithoutErrors(List<EventValidationErrors> errors) {
            when(toggleService.isLanguageRequirementsEnabled()).thenReturn(true);
            assertThat(taskListRenderer.render(tasks, errors))
                .isEqualTo(read("task-list/expected-task-list-no-errors.md"));
        }
    }

    @Nested
    class SingleCourt {

        private final List<Task> tasks = List.of(
            task(CASE_NAME, COMPLETED_FINISHED),
            task(ORDERS_SOUGHT, IN_PROGRESS),
            task(HEARING_URGENCY, COMPLETED_FINISHED),
            task(GROUNDS, COMPLETED),
            task(RISK_AND_HARM, IN_PROGRESS),
            task(APPLICATION_DOCUMENTS, COMPLETED),
            task(APPLICANT_DETAILS_LA, COMPLETED),
            task(CHILDREN, COMPLETED),
            task(RESPONDENTS, IN_PROGRESS),
            task(ALLOCATION_PROPOSAL, COMPLETED),
            task(OTHER_PROCEEDINGS, NOT_STARTED),
            task(INTERNATIONAL_ELEMENT, IN_PROGRESS),
            task(OTHERS, NOT_STARTED),
            task(COURT_SERVICES, IN_PROGRESS),
            task(SUBMIT_APPLICATION, NOT_AVAILABLE),
            task(LANGUAGE_REQUIREMENTS, COMPLETED_FINISHED)
        );

        @Test
        void shouldRenderTaskListWithApplicationDocuments() {
            when(toggleService.isLanguageRequirementsEnabled()).thenReturn(true);

            final List<EventValidationErrors> eventErrors = List.of(
                EventValidationErrors.builder()
                    .event(ORDERS_SOUGHT)
                    .errors(List.of("Add the orders and directions sought"))
                    .build());

            assertThat(taskListRenderer.render(tasks, eventErrors))
                .isEqualTo(read("task-list/expected-task-list.md"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldRenderTaskListWithoutErrors(List<EventValidationErrors> errors) {
            when(toggleService.isLanguageRequirementsEnabled()).thenReturn(true);
            assertThat(taskListRenderer.render(tasks, errors))
                .isEqualTo(read("task-list/expected-task-list-no-errors.md"));
        }
    }

    @Nested
    class MultipleCourts {

        private final List<Task> tasks = List.of(
            task(CASE_NAME, COMPLETED_FINISHED),
            task(ORDERS_SOUGHT, IN_PROGRESS),
            task(HEARING_URGENCY, COMPLETED_FINISHED),
            task(GROUNDS, COMPLETED),
            task(RISK_AND_HARM, IN_PROGRESS),
            task(APPLICATION_DOCUMENTS, COMPLETED),
            task(APPLICANT_DETAILS_LA, COMPLETED),
            task(CHILDREN, COMPLETED),
            task(RESPONDENTS, IN_PROGRESS),
            task(ALLOCATION_PROPOSAL, COMPLETED),
            task(OTHER_PROCEEDINGS, NOT_STARTED),
            task(INTERNATIONAL_ELEMENT, IN_PROGRESS),
            task(OTHERS, NOT_STARTED),
            task(COURT_SERVICES, IN_PROGRESS),
            task(SUBMIT_APPLICATION, NOT_AVAILABLE),
            task(SELECT_COURT, COMPLETED_FINISHED),
            task(LANGUAGE_REQUIREMENTS, COMPLETED_FINISHED)
        );

        @Test
        void shouldRenderTaskListWithApplicationDocuments() {
            when(toggleService.isLanguageRequirementsEnabled()).thenReturn(true);

            final List<EventValidationErrors> eventErrors = List.of(
                EventValidationErrors.builder()
                    .event(ORDERS_SOUGHT)
                    .errors(List.of("Add the orders and directions sought"))
                    .build(),
                EventValidationErrors.builder()
                    .event(SELECT_COURT)
                    .errors(List.of("Select court"))
                    .build()
            );

            assertThat(taskListRenderer.render(tasks, eventErrors))
                .isEqualTo(read("task-list/expected-task-list-multi-courts.md"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldRenderTaskListWithoutErrors(List<EventValidationErrors> errors) {
            when(toggleService.isLanguageRequirementsEnabled()).thenReturn(true);

            assertThat(taskListRenderer.render(tasks, errors))
                .isEqualTo(read("task-list/expected-task-list-no-errors-multi-courts.md"));
        }
    }

    @Nested
    class ExcludingGroundForApplication {

        private final List<Task> tasks = List.of(
            task(CASE_NAME, COMPLETED_FINISHED),
            task(ORDERS_SOUGHT, IN_PROGRESS),
            task(HEARING_URGENCY, COMPLETED_FINISHED),
            task(APPLICATION_DOCUMENTS, COMPLETED),
            task(APPLICANT_DETAILS_LA, COMPLETED),
            task(CHILDREN, COMPLETED),
            task(RESPONDENTS, IN_PROGRESS),
            task(ALLOCATION_PROPOSAL, COMPLETED),
            task(OTHER_PROCEEDINGS, NOT_STARTED),
            task(INTERNATIONAL_ELEMENT, IN_PROGRESS),
            task(OTHERS, NOT_STARTED),
            task(COURT_SERVICES, IN_PROGRESS),
            task(SUBMIT_APPLICATION, NOT_AVAILABLE),
            task(LANGUAGE_REQUIREMENTS, COMPLETED_FINISHED)
        );

        @Test
        void shouldRenderTaskList() {
            when(toggleService.isLanguageRequirementsEnabled()).thenReturn(true);

            assertThat(taskListRenderer.render(tasks, emptyList()))
                .isEqualTo(read("task-list/expected-task-list-no-grounds.md"));
        }
    }

    private static String read(String filename) {
        return readString(filename).trim();
    }
}
