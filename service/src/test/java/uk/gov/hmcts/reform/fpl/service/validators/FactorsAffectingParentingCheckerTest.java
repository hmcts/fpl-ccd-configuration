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
import uk.gov.hmcts.reform.fpl.model.FactorsParenting;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED_FINISHED;

@ExtendWith(MockitoExtension.class)
class FactorsAffectingParentingCheckerTest {

    private static final CaseData ANY_CASE_DATA = mock(CaseData.class);

    @InjectMocks
    private FactorsAffectingParentingChecker factorsAffectingParentingChecker;


    @Test
    void testValidate() {
        assertThat(factorsAffectingParentingChecker.validate(ANY_CASE_DATA)).isEmpty();
    }

    @Nested
    class IsCompleted {

        @ParameterizedTest
        @NullSource
        @MethodSource("uk.gov.hmcts.reform.fpl.service.validators"
            + ".FactorsAffectingParentingCheckerTest#incompleteFactorsAffectingParenting")
        void shouldReturnEmptyErrorsAndNonCompletedState(FactorsParenting factorsAffectingParenting) {
            final CaseData caseData = CaseData.builder()
                .factorsParenting(factorsAffectingParenting)
                .build();

            final boolean isCompleted = factorsAffectingParentingChecker.isCompleted(caseData);

            assertThat(isCompleted).isFalse();
        }

        @ParameterizedTest
        @MethodSource("uk.gov.hmcts.reform.fpl.service.validators"
            + ".FactorsAffectingParentingCheckerTest#completeFactorsAffectingParenting")
        void shouldReturnEmptyErrorsAndCompletedState(FactorsParenting factorsAffectingParenting) {
            final CaseData caseData = CaseData.builder()
                .factorsParenting(factorsAffectingParenting)
                .build();

            final boolean isCompleted = factorsAffectingParentingChecker.isCompleted(caseData);

            assertThat(isCompleted).isTrue();
        }

    }

    @Test
    void testCompletedState() {
        assertThat(factorsAffectingParentingChecker.completedState()).isEqualTo(COMPLETED_FINISHED);
    }

    private static Stream<Arguments> incompleteFactorsAffectingParenting() {
        return Stream.of(
            FactorsParenting.builder().build(),

            completedFactorsParenting()
                .alcoholDrugAbuse(null)
                .build(),
            completedFactorsParenting()
                .alcoholDrugAbuse("")
                .build(),
            completedFactorsParenting()
                .alcoholDrugAbuse("Yes")
                .alcoholDrugAbuseReason(null)
                .build(),
            completedFactorsParenting()
                .alcoholDrugAbuse("Yes")
                .alcoholDrugAbuseReason("")
                .build(),

            completedFactorsParenting()
                .domesticViolence(null)
                .build(),
            completedFactorsParenting()
                .domesticViolence("")
                .build(),
            completedFactorsParenting()
                .domesticViolence("Yes")
                .domesticViolenceReason(null)
                .build(),
            completedFactorsParenting()
                .domesticViolence("Yes")
                .domesticViolenceReason("")
                .build(),

            completedFactorsParenting()
                .anythingElse(null)
                .build(),
            completedFactorsParenting()
                .anythingElse("")
                .build(),
            completedFactorsParenting()
                .anythingElse("Yes")
                .anythingElseReason(null)
                .build(),
            completedFactorsParenting()
                .anythingElse("Yes")
                .anythingElseReason("")
                .build()
        ).map(Arguments::of);
    }

    private static Stream<Arguments> completeFactorsAffectingParenting() {
        return Stream.of(
            FactorsParenting.builder()
                .alcoholDrugAbuse("No")
                .domesticViolence("No")
                .anythingElse("No")
                .build(),
            completedFactorsParenting()
                .build())
            .map(Arguments::of);
    }

    private static FactorsParenting.FactorsParentingBuilder completedFactorsParenting() {
        return FactorsParenting.builder()
            .alcoholDrugAbuse("Yes")
            .alcoholDrugAbuseReason("Test")
            .domesticViolence("Yes")
            .domesticViolenceReason("Test")
            .anythingElse("Yes")
            .anythingElseReason("Test");
    }
}
