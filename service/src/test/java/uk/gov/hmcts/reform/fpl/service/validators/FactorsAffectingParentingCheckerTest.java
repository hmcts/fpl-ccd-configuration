package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FactorsParenting;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class FactorsAffectingParentingCheckerTest {

    @InjectMocks
    private FactorsAffectingParentingChecker factorsAffectingParentingChecker;

    @ParameterizedTest
    @NullSource
    @MethodSource("incompleteFactorsAffectingParenting")
    void shouldReturnEmptyErrorsAndNonCompletedState(FactorsParenting factorsAffectingParenting) {
        final CaseData caseData = CaseData.builder()
            .factorsParenting(factorsAffectingParenting)
            .build();

        final List<String> errors = factorsAffectingParentingChecker.validate(caseData);
        final boolean isCompleted = factorsAffectingParentingChecker.isCompleted(caseData);

        assertThat(errors).isEmpty();
        assertThat(isCompleted).isFalse();
    }

    @ParameterizedTest
    @MethodSource("completeFactorsAffectingParenting")
    void shouldReturnEmptyErrorsAndCompletedState(FactorsParenting factorsAffectingParenting) {
        final CaseData caseData = CaseData.builder()
            .factorsParenting(factorsAffectingParenting)
            .build();

        final List<String> errors = factorsAffectingParentingChecker.validate(caseData);
        final boolean isCompleted = factorsAffectingParentingChecker.isCompleted(caseData);

        assertThat(errors).isEmpty();
        assertThat(isCompleted).isTrue();
    }

    private static Stream<Arguments> incompleteFactorsAffectingParenting() {
        return Stream.of(
            FactorsParenting.builder().build(),
            FactorsParenting.builder()
                .alcoholDrugAbuse("")
                .domesticViolence("")
                .anythingElse("")
                .build(),
            FactorsParenting.builder()
                .alcoholDrugAbuse("Yes")
                .domesticViolence("No")
                .anythingElse("No")
                .build(),
            FactorsParenting.builder()
                .alcoholDrugAbuse("No")
                .domesticViolence("Yes")
                .anythingElse("No")
                .build(),
            FactorsParenting.builder()
                .alcoholDrugAbuse("No")
                .domesticViolence("No")
                .anythingElse("Yes")
                .build())
            .map(Arguments::of);
    }

    private static Stream<Arguments> completeFactorsAffectingParenting() {
        return Stream.of(
            FactorsParenting.builder()
                .alcoholDrugAbuse("No")
                .domesticViolence("No")
                .anythingElse("No")
                .build(),
            FactorsParenting.builder()
                .alcoholDrugAbuse("Yes")
                .alcoholDrugAbuseReason("Test")
                .domesticViolence("Yes")
                .domesticViolenceReason("Test")
                .anythingElse("Yes")
                .anythingElseReason("Test")
                .build())
            .map(Arguments::of);
    }
}
