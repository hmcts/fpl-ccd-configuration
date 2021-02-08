package uk.gov.hmcts.reform.fpl.service.tasklist;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.fpl.enums.Event;
import uk.gov.hmcts.reform.fpl.model.tasklist.Task;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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

class TaskListCompletedMarkGeneratorTest {

    private static final Task TASK = mock(Task.class);
    private static final String LINK = "Link";
    private static final String IMAGE = "Image";
    private static final List<Event> MARKED_AS_FINISHED = List.of(
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

    private final FeatureToggleService featureToggleService = mock(FeatureToggleService.class);
    private final TaskListRenderElements taskListRenderElements = mock(TaskListRenderElements.class);

    private final TaskListCompletedMarkGenerator underTest = new TaskListCompletedMarkGenerator(
        featureToggleService,
        taskListRenderElements);


    @Test
    void testGenerateIfToggleOff() {

        when(featureToggleService.isFinishedTagEnabled()).thenReturn(false);
        when(taskListRenderElements.renderLink(TASK)).thenReturn(LINK);
        when(taskListRenderElements.renderImage("information-added.png", "Information added")).thenReturn(IMAGE);

        String actual = underTest.generate(TASK);

        assertThat(actual).isEqualTo(LINK + IMAGE);

    }

    @ParameterizedTest
    @MethodSource("markedAsFinished")
    void testGenerateIfTaskTypeMarkedForFinishedToggleOn(Event event) {
        Task task = Task.builder().event(event).build();

        when(featureToggleService.isFinishedTagEnabled()).thenReturn(true);
        when(taskListRenderElements.renderLink(task)).thenReturn(LINK);
        when(taskListRenderElements.renderImage("finished.png", "Finished")).thenReturn(IMAGE);

        String actual = underTest.generate(task);

        assertThat(actual).isEqualTo(LINK + IMAGE);
    }

    @ParameterizedTest
    @MethodSource("notMarkedAsFinished")
    void testGenerateIfTaskTypeNotMarkedForFinishedToggleOn(Event event) {
        Task task = Task.builder().event(event).build();

        when(featureToggleService.isFinishedTagEnabled()).thenReturn(true);
        when(taskListRenderElements.renderLink(task)).thenReturn(LINK);
        when(taskListRenderElements.renderImage("information-added.png", "Information added")).thenReturn(IMAGE);

        String actual = underTest.generate(task);

        assertThat(actual).isEqualTo(LINK + IMAGE);
    }

    private static Stream<Arguments> markedAsFinished() {
        return MARKED_AS_FINISHED.stream().map(Arguments::of);
    }

    private static Stream<Arguments> notMarkedAsFinished() {
        return Arrays.stream(Event.values()).filter(o -> !MARKED_AS_FINISHED.contains(o)).map(Arguments::of);
    }
}
