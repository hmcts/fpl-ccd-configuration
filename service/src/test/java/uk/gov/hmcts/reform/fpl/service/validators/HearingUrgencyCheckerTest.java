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

import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.enums.hearing.HearingUrgencyType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Hearing;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
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
                    .build()).build();

            final List<String> errors = hearingUrgencyChecker.validate(caseData);

            assertThat(errors).isEmpty();
        }

        @Test
        void shouldReturnErrorWhenNoHearingNeedsProvided() {
            final CaseData caseData = CaseData.builder().build();

            final List<String> errors = hearingUrgencyChecker.validate(caseData);

            assertThat(errors).containsExactly("Add the hearing urgency details");
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
                .hearingUrgencyType(null)
                .build(),
            completedHearing()
                .hearingUrgencyType(HearingUrgencyType.URGENT)
                .hearingUrgencyDetails(null)
                .build(),
            completedHearing()
                .hearingUrgencyType(HearingUrgencyType.URGENT)
                .hearingUrgencyDetails("")
                .build(),
            completedHearing()
                .hearingUrgencyType(HearingUrgencyType.SAME_DAY)
                .hearingUrgencyDetails(null)
                .build(),
            completedHearing()
                .hearingUrgencyType(HearingUrgencyType.SAME_DAY)
                .hearingUrgencyDetails("")
                .build(),
            completedHearing()
                .withoutNotice(null)
                .build(),
            completedHearing()
                .withoutNotice(YES.getValue())
                .withoutNoticeReason(null)
                .build(),
            completedHearing()
                .withoutNotice(YES.getValue())
                .withoutNoticeReason("")
                .build(),
            completedHearing()
                .respondentsAware(null)
                .build(),
            completedHearing()
                .respondentsAware(NO.getValue())
                .respondentsAwareReason(null)
                .build(),
            completedHearing()
                .respondentsAware(NO.getValue())
                .respondentsAwareReason("")
                .build()
        ).map(Arguments::of);
    }

    private static Stream<Arguments> completeHearing() {
        return Stream.of(
            completedHearing()
                .hearingUrgencyType(HearingUrgencyType.STANDARD)
                .hearingUrgencyDetails(null)
                .build(),
            completedHearing()
                .respondentsAware(YES.getValue())
                .respondentsAwareReason(null)
                .build(),
            completedHearing()
                .withoutNotice(NO.getValue())
                .withoutNoticeReason(null)
                .build(),
            completedHearing()
                .build()
        )
            .map(Arguments::of);
    }

    private static Hearing.HearingBuilder completedHearing() {
        return Hearing.builder()
            .hearingUrgencyType(HearingUrgencyType.URGENT)
            .hearingUrgencyDetails("Test")
            .withoutNotice(YES.getValue())
            .withoutNoticeReason("Test")
            .respondentsAware(NO.getValue())
            .respondentsAwareReason("Test");
    }
}
