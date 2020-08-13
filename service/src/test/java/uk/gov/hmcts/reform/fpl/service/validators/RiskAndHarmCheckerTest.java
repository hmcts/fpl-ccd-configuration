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

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RiskAndHarmCheckerTest {

    @InjectMocks
    private RiskAndHarmChecker riskAndHarmChecker;

    @ParameterizedTest
    @NullSource
    @MethodSource("risks")
    void shouldReturnEmptyErrorsAndNonCompletedStateForOptionalEvent(Risks risks) {
        final CaseData caseData = CaseData.builder()
                .risks(risks)
                .build();

        final List<String> errors = riskAndHarmChecker.validate(caseData);
        final boolean isCompleted = riskAndHarmChecker.isCompleted(caseData);

        assertThat(errors).isEmpty();
        assertThat(isCompleted).isFalse();
    }

    private static Stream<Arguments> risks() {
        return Stream.of(
                Risks.builder().build(),
                Risks.builder()
                        .emotionalHarm("")
                        .physicalHarm("")
                        .sexualAbuse("")
                        .neglect("")
                        .build(),
                Risks.builder()
                        .emotionalHarm("Yes")
                        .physicalHarm("No")
                        .sexualAbuse("Yes")
                        .neglect("No")
                        .build())
                .map(Arguments::of);
    }
}
