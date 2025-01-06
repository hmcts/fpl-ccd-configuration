package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Hearing;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED_FINISHED;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HearingUrgencyChecker.class, LocalValidatorFactoryBean.class})
class HearingUrgencyCheckerTest {

    @Autowired
    private HearingUrgencyChecker hearingUrgencyChecker;

    @Nested
    class Validate {

        @Test
        void shouldReturnEmptyErrorsAndCompletedState() {
            final CaseData caseData = CaseData.builder()
                .hearing(Hearing.builder()
                    .timeFrame("Within 18 days")
                    .build()).build();

            final List<String> errors = hearingUrgencyChecker.validate(caseData);

            assertThat(errors).isEmpty();
        }

        @Test
        void shouldReturnErrorWhenNoHearingNeedsProvided() {
            final CaseData caseData = CaseData.builder().build();

            final List<String> errors = hearingUrgencyChecker.validate(caseData);

            assertThat(errors)
                .containsExactly("Add the hearing urgency details|Ychwanegu manylion brys y gwrandawiad");
        }

        @Test
        void shouldReturnErrorWhenHearingTimeFrameIsNotProvided() {
            final CaseData caseData = CaseData.builder()
                .hearing(Hearing.builder().build())
                .build();

            final List<String> errors = hearingUrgencyChecker.validate(caseData);

            assertThat(errors).containsExactly("Select an option for when you need a hearing");
        }

        @Test
        void shouldReturnEmptyErrorsWhenHearingTimeFrameIsProvided() {
            final CaseData caseData = CaseData.builder()
                .hearing(Hearing.builder()
                    .timeFrame("Within 18 days")
                    .build())
                .build();

            final List<String> errors = hearingUrgencyChecker.validate(caseData);

            assertThat(errors).isEmpty();
        }
    }

    @Nested
    class IsCompleted {

        @ParameterizedTest
        @MethodSource("uk.gov.hmcts.reform.fpl.service.validators.HearingUrgencyCheckerTest#incompleteHearing")
        void shouldReturnEmptyErrorsAndNonCompletedState(Hearing hearing) {
            final CaseData caseData = CaseData.builder().hearing(hearing).build();

            final boolean isCompleted = hearingUrgencyChecker.isCompleted(caseData);

            assertThat(isCompleted).isFalse();
        }

        @ParameterizedTest
        @MethodSource("uk.gov.hmcts.reform.fpl.service.validators.HearingUrgencyCheckerTest#completeHearing")
        void shouldReturnEmptyErrorsAndCompletedState(Hearing hearing) {
            final CaseData caseData = CaseData.builder().hearing(hearing).build();

            final boolean isCompleted = hearingUrgencyChecker.isCompleted(caseData);

            assertThat(isCompleted).isTrue();
        }

        @Test
        void shouldReturnErrorWhenNoHearingNeedsProvided() {
            final CaseData caseData = CaseData.builder().build();

            final boolean isCompleted = hearingUrgencyChecker.isCompleted(caseData);

            assertThat(isCompleted).isFalse();
        }
    }

    @Test
    void testCompletedState() {
        assertThat(hearingUrgencyChecker.completedState()).isEqualTo(COMPLETED_FINISHED);
    }

    private static Stream<Arguments> incompleteHearing() {
        return Stream.of(
            completedHearing()
                .timeFrame(null)
                .build(),
            completedHearing()
                .timeFrame("")
                .build(),
            completedHearing()
                .timeFrame("Same day")
                .reason(null)
                .build(),
            completedHearing()
                .timeFrame("Same day")
                .reason("")
                .build(),

            completedHearing()
                .type(null)
                .build(),
            completedHearing()
                .type("")
                .build(),

            completedHearing()
                .withoutNotice(null)
                .build(),
            completedHearing()
                .withoutNotice("")
                .build(),
            completedHearing()
                .withoutNotice("Yes")
                .withoutNoticeReason(null)
                .build(),
            completedHearing()
                .withoutNotice("Yes")
                .withoutNoticeReason("")
                .build(),

            completedHearing()
                .reducedNotice(null)
                .build(),
            completedHearing()
                .reducedNotice("")
                .build(),
            completedHearing()
                .reducedNotice("Yes")
                .reducedNoticeReason(null)
                .build(),
            completedHearing()
                .reducedNotice("Yes")
                .reducedNoticeReason("")
                .build(),

            completedHearing()
                .respondentsAware(null)
                .build(),
            completedHearing()
                .respondentsAware("")
                .build(),
            completedHearing()
                .respondentsAware("Yes")
                .respondentsAwareReason(null)
                .build(),
            completedHearing()
                .respondentsAware("Yes")
                .respondentsAwareReason("")
                .build(),

            completedHearing()
                .respondentsAware(null)
                .build()
        ).map(Arguments::of);
    }

    private static Stream<Arguments> completeHearing() {
        return Stream.of(
            Hearing.builder()
                .timeFrame("Within 18 days")
                .type("Standard case management hearing")
                .typeGiveReason("Test")
                .withoutNotice("No")
                .reducedNotice("No")
                .respondentsAware("No")
                .build(),
            completedHearing()
                .build()
        )
            .map(Arguments::of);
    }

    private static Hearing.HearingBuilder completedHearing() {
        return Hearing.builder()
            .timeFrame("Same day")
            .reason("Test")
            .type("Standard case management hearing")
            .typeGiveReason("Test")
            .withoutNotice("Yes")
            .withoutNoticeReason("Test")
            .reducedNotice("No")
            .reducedNoticeReason("Test")
            .respondentsAware("No")
            .respondentsAwareReason("Test");
    }
}
