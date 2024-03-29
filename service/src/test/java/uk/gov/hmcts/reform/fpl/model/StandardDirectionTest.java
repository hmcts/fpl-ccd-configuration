package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.configuration.Display;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionDueDateType.DAYS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.APPOINT_CHILDREN_GUARDIAN;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.APPOINT_CHILDREN_GUARDIAN_IMMEDIATE;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.ARRANGE_INTERPRETERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionType.ARRANGE_INTERPRETERS_IMMEDIATE;

class StandardDirectionTest {

    private static final DirectionConfiguration DIRECTION_CONFIGURATION = DirectionConfiguration.builder()
        .type(APPOINT_CHILDREN_GUARDIAN)
        .assignee(CAFCASS)
        .title("test title")
        .text("test text")
        .display(Display.builder()
            .showDateOnly(false)
            .delta("-2")
            .build())
        .build();

    @Test
    void shouldAddFieldsFromOrderConfiguration() {
        final StandardDirection initialStandardDirection = StandardDirection.builder().build();

        final StandardDirection expectedStandardDirection = StandardDirection.builder()
            .type(APPOINT_CHILDREN_GUARDIAN)
            .assignee(CAFCASS)
            .title("test title")
            .description("test text")
            .daysBeforeHearing(2)
            .dueDateType(null)
            .dateToBeCompletedBy(null)
            .build();

        final StandardDirection actualStandardDirection = initialStandardDirection.applyConfig(DIRECTION_CONFIGURATION);

        assertThat(actualStandardDirection).isEqualTo(expectedStandardDirection);
    }

    @Test
    void shouldAddNonExistingFieldsFromOrderConfiguration() {

        final LocalDateTime dueDate = LocalDateTime.now();

        final StandardDirection initialStandardDirection = StandardDirection.builder()
            .description("new text")
            .dueDateType(DAYS)
            .dateToBeCompletedBy(dueDate)
            .daysBeforeHearing(3)
            .build();

        final StandardDirection expectedStandardDirection = StandardDirection.builder()
            .type(APPOINT_CHILDREN_GUARDIAN)
            .title("test title")
            .description("new text")
            .assignee(CAFCASS)
            .dueDateType(DAYS)
            .daysBeforeHearing(3)
            .dateToBeCompletedBy(dueDate)
            .build();

        final StandardDirection actualStandardDirection = initialStandardDirection.applyConfig(DIRECTION_CONFIGURATION);

        assertThat(actualStandardDirection).isEqualTo(expectedStandardDirection);
    }

    @Test
    void shouldCreateStandardDirectionWithoutDaysBeforeHearingIfImmediateStandardDirection() {

        final DirectionConfiguration immediateStandardDirectionConfig = DirectionConfiguration.builder()
            .type(APPOINT_CHILDREN_GUARDIAN_IMMEDIATE)
            .assignee(CAFCASS)
            .title("test title")
            .text("test text")
            .display(Display.builder()
                .showDateOnly(false)
                .delta("-2")
                .build())
            .build();

        final StandardDirection initialStandardDirection = StandardDirection.builder()
            .description("new text")
            .build();

        final StandardDirection expectedStandardDirection = StandardDirection.builder()
            .type(APPOINT_CHILDREN_GUARDIAN_IMMEDIATE)
            .title("test title")
            .description("new text")
            .assignee(CAFCASS)
            .build();

        final StandardDirection actualStandardDirection =
            initialStandardDirection.applyConfig(immediateStandardDirectionConfig);

        assertThat(actualStandardDirection).isEqualTo(expectedStandardDirection);
    }

    @Test
    void isImmediateStandardDirectionShouldReturnTrueForImmediateStandardDirectionTypes() {
        final StandardDirection arrangeInterpretersImmediate = StandardDirection.builder()
            .type(ARRANGE_INTERPRETERS_IMMEDIATE)
            .build();

        final StandardDirection appointChildrenGuardianImmediate = StandardDirection.builder()
            .type(APPOINT_CHILDREN_GUARDIAN_IMMEDIATE)
            .build();

        assertThat(arrangeInterpretersImmediate.isImmediateStandardDirection(ARRANGE_INTERPRETERS_IMMEDIATE))
            .isEqualTo(true);
        assertThat(appointChildrenGuardianImmediate.isImmediateStandardDirection(APPOINT_CHILDREN_GUARDIAN_IMMEDIATE))
            .isEqualTo(true);
    }

    @Test
    void isImmediateStandardDirectionShouldReturnFalseForNonImmediateStandardDirectionTypes() {
        final StandardDirection arrangeInterpreters = StandardDirection.builder()
            .type(ARRANGE_INTERPRETERS)
            .build();

        final StandardDirection appointChildrenGuardian = StandardDirection.builder()
            .type(APPOINT_CHILDREN_GUARDIAN)
            .build();

        assertThat(arrangeInterpreters.isImmediateStandardDirection(ARRANGE_INTERPRETERS)).isEqualTo(false);
        assertThat(appointChildrenGuardian.isImmediateStandardDirection(APPOINT_CHILDREN_GUARDIAN)).isEqualTo(false);
    }
}
