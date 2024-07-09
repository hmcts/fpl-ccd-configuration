package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Risks;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RiskAndHarmCheckerIsStartedTest {

    @InjectMocks
    private RiskAndHarmChecker riskAndHarmChecker;

    @ParameterizedTest
    @NullSource
    @MethodSource("emptyRisks")
    void shouldReturnFalseWhenEmptyRisks(Risks risks) {
        final CaseData caseData = CaseData.builder()
                .risks(risks)
                .build();

        assertThat(riskAndHarmChecker.isStarted(caseData)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("nonEmptyRisks")
    void shouldReturnTrueWhenNonEmptyRisks(Risks risks) {
        final CaseData caseData = CaseData.builder()
                .risks(risks)
                .build();

        assertThat(riskAndHarmChecker.isStarted(caseData)).isTrue();
    }

    private static Stream<Arguments> nonEmptyRisks() {
        return Stream.of(
                Risks.builder().whatKindOfRiskAndHarmToChildren(List.of("Emotional harm")).build(),
                Risks.builder().factorsAffectingParenting(List.of("Anything else")).build(),
                Risks.builder().anythingElseAffectingParenting("Something else").build())
                .map(Arguments::of);
    }

    private static Stream<Arguments> emptyRisks() {
        return Stream.of(
                Risks.builder().build(),
                Risks.builder()
                        .whatKindOfRiskAndHarmToChildren(emptyList())
                        .factorsAffectingParenting(emptyList())
                        .anythingElseAffectingParenting("")
                        .build())
                .map(Arguments::of);
    }
}
