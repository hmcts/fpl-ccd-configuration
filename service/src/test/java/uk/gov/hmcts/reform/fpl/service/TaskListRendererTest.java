package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.gov.hmcts.reform.fpl.model.submission.EventValidationErrors;
import uk.gov.hmcts.reform.fpl.model.tasklist.Task;
import uk.gov.hmcts.reform.fpl.service.tasklist.TaskListRenderElements;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.Event.ALLOCATION_PROPOSAL;
import static uk.gov.hmcts.reform.fpl.enums.Event.APPLICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.fpl.enums.Event.CHILDREN;
import static uk.gov.hmcts.reform.fpl.enums.Event.COURT_SERVICES;
import static uk.gov.hmcts.reform.fpl.enums.Event.FACTORS_AFFECTING_PARENTING;
import static uk.gov.hmcts.reform.fpl.enums.Event.GROUNDS;
import static uk.gov.hmcts.reform.fpl.enums.Event.HEARING_URGENCY;
import static uk.gov.hmcts.reform.fpl.enums.Event.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.fpl.enums.Event.LANGUAGE_REQUIREMENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.LOCAL_AUTHORITY_DETAILS;
import static uk.gov.hmcts.reform.fpl.enums.Event.ORDERS_SOUGHT;
import static uk.gov.hmcts.reform.fpl.enums.Event.ORGANISATION_DETAILS;
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
import static uk.gov.hmcts.reform.fpl.model.tasklist.ValidationErrorMessages.ADD_ORDERS_DIRECTIONS;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readString;

class TaskListRendererTest {

    private static final Long CASE_ID = 1L;

    private final FeatureToggleService toggleService = mock(FeatureToggleService.class);
    private final TemplateRenderer templateRenderer = new TemplateRenderer();
    private final TaskListRenderer taskListRenderer = new TaskListRenderer(
        new TaskListRenderElements(
            "https://raw.githubusercontent.com/hmcts/fpl-ccd-configuration/master/resources/"
        ), toggleService, templateRenderer);

    TaskListRendererTest() {

    }

    @Nested
    class WithLegacyApplicant {

        private final List<Task> tasks = List.of(
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
            task(SUBMIT_APPLICATION, NOT_AVAILABLE),
            task(LANGUAGE_REQUIREMENTS, COMPLETED_FINISHED));

        @Test
        @Deprecated
        void shouldRenderTaskListWithApplicationDocumentsCode() {
            when(toggleService.isLanguageRequirementsEnabled()).thenReturn(true);
            List<EventValidationErrors> eventErrors = List.of(
                EventValidationErrors.builder()
                    .event(ORDERS_SOUGHT)
                    .errors(List.of("Add the orders and directions sought"))
                    .build());

            assertThat(taskListRenderer.render(tasks, eventErrors))
                .isEqualTo(read("task-list/deprecated/legacy-applicant/expected-task-list.md"));
        }

        @Test
        void shouldRenderTaskListWithApplicationDocumentsFreemarker() {
            when(toggleService.isLanguageRequirementsEnabled()).thenReturn(true);
            List<EventValidationErrors> eventErrors = List.of(
                EventValidationErrors.builder()
                    .event(ORDERS_SOUGHT)
                    .errors(List.of(ADD_ORDERS_DIRECTIONS))
                    .build());

            assertThat(taskListRenderer.renderTasks(tasks, eventErrors, CASE_ID))
                .isEqualTo(read("task-list/freemarker/legacy-applicant/expected-task-list.md"));
        }

        @Test
        void shouldRenderWelshTaskListWithApplicationDocumentsFreemarker() {
            when(toggleService.isLanguageRequirementsEnabled()).thenReturn(true);
            List<EventValidationErrors> eventErrors = List.of(
                EventValidationErrors.builder()
                    .event(ORDERS_SOUGHT)
                    .errors(List.of(ADD_ORDERS_DIRECTIONS))
                    .build());

            assertThat(taskListRenderer.renderTasks(tasks, eventErrors, Optional.empty(), Optional.empty(), CASE_ID, true))
                .isEqualTo(read("task-list/freemarker/legacy-applicant/expected-task-list-welsh.md"));
        }


        @ParameterizedTest
        @NullAndEmptySource
        @Deprecated
        void shouldRenderTaskListWithoutErrorsCode(List<EventValidationErrors> errors) {
            when(toggleService.isLanguageRequirementsEnabled()).thenReturn(true);
            assertThat(taskListRenderer.render(tasks, errors))
                .isEqualTo(read("task-list/deprecated/legacy-applicant/expected-task-list-no-errors.md"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldRenderTaskListWithoutErrorsFreemarker(List<EventValidationErrors> errors) {
            when(toggleService.isLanguageRequirementsEnabled()).thenReturn(true);
            assertThat(taskListRenderer.renderTasks(tasks, errors, CASE_ID))
                .isEqualTo(read("task-list/freemarker/legacy-applicant/expected-task-list-no-errors.md"));
        }

    }

    @Nested
    class WithLocalAuthority {
        private final List<Task> tasks = List.of(
            task(CASE_NAME, COMPLETED_FINISHED),
            task(ORDERS_SOUGHT, IN_PROGRESS),
            task(HEARING_URGENCY, COMPLETED_FINISHED),
            task(GROUNDS, COMPLETED),
            task(RISK_AND_HARM, IN_PROGRESS),
            task(FACTORS_AFFECTING_PARENTING, COMPLETED_FINISHED),
            task(APPLICATION_DOCUMENTS, COMPLETED),
            task(LOCAL_AUTHORITY_DETAILS, COMPLETED),
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
        @Deprecated
        void shouldRenderTaskListWithApplicationDocumentsCode() {
            when(toggleService.isLanguageRequirementsEnabled()).thenReturn(true);
            List<EventValidationErrors> eventErrors = List.of(
                EventValidationErrors.builder()
                    .event(ORDERS_SOUGHT)
                    .errors(List.of("Add the orders and directions sought"))
                    .build());

            assertThat(taskListRenderer.render(tasks, eventErrors))
                .isEqualTo(read("task-list/deprecated/expected-task-list.md"));
        }

        @Test
        void shouldRenderTaskListWithApplicationDocumentsFreemarker() {
            when(toggleService.isLanguageRequirementsEnabled()).thenReturn(true);
            List<EventValidationErrors> eventErrors = List.of(
                EventValidationErrors.builder()
                    .event(ORDERS_SOUGHT)
                    .errors(List.of(ADD_ORDERS_DIRECTIONS))
                    .build());

            assertThat(taskListRenderer.renderTasks(tasks, eventErrors, CASE_ID))
                .isEqualTo(read("task-list/freemarker/expected-task-list.md"));
        }

        @Test
        void shouldRenderWelshTaskListWithApplicationDocumentsFreemarker() {
            when(toggleService.isLanguageRequirementsEnabled()).thenReturn(true);
            List<EventValidationErrors> eventErrors = List.of(
                EventValidationErrors.builder()
                    .event(ORDERS_SOUGHT)
                    .errors(List.of(ADD_ORDERS_DIRECTIONS))
                    .build());

            assertThat(taskListRenderer.renderTasks(tasks, eventErrors, Optional.empty(),
                Optional.empty(), CASE_ID, true))
                .isEqualTo(read("task-list/freemarker/expected-task-list-welsh.md"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @Deprecated
        void shouldRenderTaskListWithoutErrorsCode(List<EventValidationErrors> errors) {
            when(toggleService.isLanguageRequirementsEnabled()).thenReturn(true);
            assertThat(taskListRenderer.render(tasks, errors))
                .isEqualTo(read("task-list/deprecated/expected-task-list-no-errors.md"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldRenderTaskListWithoutErrorsFreemarker(List<EventValidationErrors> errors) {
            when(toggleService.isLanguageRequirementsEnabled()).thenReturn(true);
            assertThat(taskListRenderer.renderTasks(tasks, errors, CASE_ID))
                .isEqualTo(read("task-list/freemarker/expected-task-list-no-errors.md"));
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
            task(FACTORS_AFFECTING_PARENTING, COMPLETED_FINISHED),
            task(APPLICATION_DOCUMENTS, COMPLETED),
            task(LOCAL_AUTHORITY_DETAILS, COMPLETED),
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
        @Deprecated
        void shouldRenderTaskListWithApplicationDocumentsCode() {
            when(toggleService.isLanguageRequirementsEnabled()).thenReturn(true);

            final List<EventValidationErrors> eventErrors = List.of(
                EventValidationErrors.builder()
                    .event(ORDERS_SOUGHT)
                    .errors(List.of("Add the orders and directions sought"))
                    .build());

            assertThat(taskListRenderer.render(tasks, eventErrors))
                .isEqualTo(read("task-list/deprecated/expected-task-list.md"));
        }

        @Test
        void shouldRenderTaskListWithApplicationDocumentsFreemarker() {
            when(toggleService.isLanguageRequirementsEnabled()).thenReturn(true);

            final List<EventValidationErrors> eventErrors = List.of(
                EventValidationErrors.builder()
                    .event(ORDERS_SOUGHT)
                    .errors(List.of("Add the orders and directions sought"))
                    .build());

            assertThat(taskListRenderer.renderTasks(tasks, eventErrors, CASE_ID))
                .isEqualTo(read("task-list/freemarker/expected-task-list.md"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @Deprecated
        void shouldRenderTaskListWithoutErrorsCode(List<EventValidationErrors> errors) {
            when(toggleService.isLanguageRequirementsEnabled()).thenReturn(true);
            assertThat(taskListRenderer.render(tasks, errors))
                .isEqualTo(read("task-list/deprecated/expected-task-list-no-errors.md"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldRenderTaskListWithoutErrorsFreemarker(List<EventValidationErrors> errors) {
            when(toggleService.isLanguageRequirementsEnabled()).thenReturn(true);
            assertThat(taskListRenderer.renderTasks(tasks, errors, CASE_ID))
                .isEqualTo(read("task-list/freemarker/expected-task-list-no-errors.md"));
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
            task(FACTORS_AFFECTING_PARENTING, COMPLETED_FINISHED),
            task(APPLICATION_DOCUMENTS, COMPLETED),
            task(LOCAL_AUTHORITY_DETAILS, COMPLETED),
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
        @Deprecated
        void shouldRenderTaskListWithApplicationDocumentsCode() {
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
                .isEqualTo(read("task-list/deprecated/expected-task-list-multi-courts.md"));
        }

        @Test
        void shouldRenderTaskListWithApplicationDocumentsFreemarker() {
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

            assertThat(taskListRenderer.renderTasks(tasks, eventErrors, CASE_ID))
                .isEqualTo(read("task-list/freemarker/expected-task-list-multi-courts.md"));
        }


        @ParameterizedTest
        @NullAndEmptySource
        @Deprecated
        void shouldRenderTaskListWithoutErrorsCode(List<EventValidationErrors> errors) {
            when(toggleService.isLanguageRequirementsEnabled()).thenReturn(true);

            assertThat(taskListRenderer.render(tasks, errors))
                .isEqualTo(read("task-list/deprecated/expected-task-list-no-errors-multi-courts.md"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldRenderTaskListWithoutErrorsFreemarker(List<EventValidationErrors> errors) {
            when(toggleService.isLanguageRequirementsEnabled()).thenReturn(true);

            assertThat(taskListRenderer.renderTasks(tasks, errors, CASE_ID))
                .isEqualTo(read("task-list/freemarker/expected-task-list-no-errors-multi-courts.md"));
        }

    }

    @Nested
    class ExcludingGroundForApplication {

        private final List<Task> tasks = List.of(
            task(CASE_NAME, COMPLETED_FINISHED),
            task(ORDERS_SOUGHT, IN_PROGRESS),
            task(HEARING_URGENCY, COMPLETED_FINISHED),
            task(APPLICATION_DOCUMENTS, COMPLETED),
            task(LOCAL_AUTHORITY_DETAILS, COMPLETED),
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
        @Deprecated
        void shouldRenderTaskListCode() {
            when(toggleService.isLanguageRequirementsEnabled()).thenReturn(true);

            assertThat(taskListRenderer.render(tasks, emptyList()))
                .isEqualTo(read("task-list/deprecated/expected-task-list-no-grounds.md"));
        }

        @Test
        void shouldRenderTaskListFreemarker() {
            when(toggleService.isLanguageRequirementsEnabled()).thenReturn(true);

            assertThat(taskListRenderer.renderTasks(tasks, emptyList(), CASE_ID))
                .isEqualTo(read("task-list/freemarker/expected-task-list-no-grounds.md"));
        }

    }

    private static String read(String filename) {
        return readString(filename).trim();
    }
}
