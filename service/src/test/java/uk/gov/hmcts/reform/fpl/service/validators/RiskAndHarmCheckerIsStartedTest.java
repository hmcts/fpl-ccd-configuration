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

import java.util.stream.Stream;

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
                Risks.builder().neglect("Yes").build(),
                Risks.builder().sexualAbuse("No").build(),
                Risks.builder().physicalHarm("Yes").build(),
                Risks.builder().emotionalHarm("No").build())
                .map(Arguments::of);
    }

    private static Stream<Arguments> emptyRisks() {
        return Stream.of(
                Risks.builder().build(),
                Risks.builder()
                        .neglect("")
                        .sexualAbuse("")
                        .physicalHarm("")
                        .emotionalHarm("")
                        .build())
                .map(Arguments::of);
    }
}
