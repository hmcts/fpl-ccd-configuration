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

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class FactorsAffectingParentingCheckerIsStartedTest {

    @InjectMocks
    private FactorsAffectingParentingChecker factorsAffectingParentingChecker;

    @ParameterizedTest
    @NullSource
    @MethodSource("emptyFactorsAffectingParenting")
    void shouldReturnFalseWhenEmptyFactorsAffecting(final FactorsParenting factors) {
        final CaseData caseData = CaseData.builder()
                .factorsParenting(factors)
                .build();

        assertThat(factorsAffectingParentingChecker.isStarted(caseData)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("nonEmptyFactorsAffectingParenting")
    void shouldReturnTrueWhenNonEmptyFactorsAffectingParenting(final FactorsParenting factors) {
        final CaseData caseData = CaseData.builder()
                .factorsParenting(factors)
                .build();

        assertThat(factorsAffectingParentingChecker.isStarted(caseData)).isTrue();
    }

    private static Stream<Arguments> nonEmptyFactorsAffectingParenting() {
        return Stream.of(
                FactorsParenting.builder()
                        .alcoholDrugAbuse("Yes")
                        .build(),
                FactorsParenting.builder()
                        .domesticViolence("No")
                        .build(),
                FactorsParenting.builder()
                        .anythingElse("Yes")
                        .build())
                .map(Arguments::of);
    }

    private static Stream<Arguments> emptyFactorsAffectingParenting() {
        return Stream.of(
                FactorsParenting.builder().build(),
                FactorsParenting.builder()
                        .alcoholDrugAbuse("")
                        .domesticViolence("")
                        .anythingElse("")
                        .build())
                .map(Arguments::of);
    }
}
