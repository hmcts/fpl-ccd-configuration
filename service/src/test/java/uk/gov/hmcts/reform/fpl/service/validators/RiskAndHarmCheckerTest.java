package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Risks;

import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED_FINISHED;

@ExtendWith(MockitoExtension.class)
class RiskAndHarmCheckerTest {

    private static final CaseData ANY_CASE_DATA = mock(CaseData.class);

    @InjectMocks
    private RiskAndHarmChecker riskAndHarmChecker;

    @Test
    void testValidate() {
        assertThat(riskAndHarmChecker.validate(ANY_CASE_DATA)).isEmpty();
    }

    @Test
    void testCompletedState() {
        assertThat(riskAndHarmChecker.completedState()).isEqualTo(COMPLETED_FINISHED);
    }

    @Nested
    class IsCompleted {

        @ParameterizedTest
        @NullSource
        @MethodSource("uk.gov.hmcts.reform.fpl.service.validators.RiskAndHarmCheckerTest#incompleteRisks")
        void shouldReturnEmptyErrorsAndNonCompletedState(Risks risks) {
            final CaseData caseData = CaseData.builder()
                .risks(risks)
                .build();

            final boolean isCompleted = riskAndHarmChecker.isCompleted(caseData);

            assertThat(isCompleted).isFalse();
        }

        @ParameterizedTest
        @MethodSource("uk.gov.hmcts.reform.fpl.service.validators.RiskAndHarmCheckerTest#completeRisks")
        void shouldReturnEmptyErrorsAndCompletedState(Risks risks) {
            final CaseData caseData = CaseData.builder()
                .risks(risks)
                .build();

            final boolean isCompleted = riskAndHarmChecker.isCompleted(caseData);

            assertThat(isCompleted).isTrue();
        }
    }

    private static Stream<Arguments> incompleteRisks() {
        return Stream.of(
            Risks.builder().build(),
            completedRisk()
                .emotionalHarmOccurrences(null)
                .build(),
            completedRisk()
                .emotionalHarmOccurrences(emptyList())
                .build(),

            completedRisk()
                .physicalHarmOccurrences(null)
                .build(),
            completedRisk()
                .physicalHarmOccurrences(emptyList())
                .build(),

            completedRisk()
                .sexualAbuseOccurrences(null)
                .build(),
            completedRisk()
                .sexualAbuseOccurrences(emptyList())
                .build(),

            completedRisk()
                .neglectOccurrences(null)
                .build(),
            completedRisk()
                .neglectOccurrences(emptyList())
                .build()
        ).map(Arguments::of);
    }

    private static Stream<Arguments> completeRisks() {
        return Stream.of(
            Risks.builder()
                .emotionalHarm("No")
                .physicalHarm("No")
                .sexualAbuse("No")
                .neglect("No")
                .build(),
            completedRisk()
                .build()
        ).map(Arguments::of);
    }

    private static Risks.RisksBuilder completedRisk() {
        return Risks.builder()
            .emotionalHarm("Yes")
            .emotionalHarmOccurrences(singletonList("Past harm"))
            .physicalHarm("Yes")
            .physicalHarmOccurrences(singletonList("Past harm"))
            .sexualAbuse("Yes")
            .sexualAbuseOccurrences(singletonList("Past harm"))
            .neglect("Yes")
            .neglectOccurrences(singletonList("Past harm"));
    }
}
