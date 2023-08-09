package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Proceeding;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ProceedingsCheckerIsStartedTest {

    @InjectMocks
    private ProceedingsChecker proceedingsChecker;

    @ParameterizedTest
    @NullSource
    @MethodSource("emptyProceedings")
    void shouldReturnFalseWhenEmptyProceedings(Proceeding proceeding) {
        final CaseData caseData = CaseData.builder()
                .proceeding(proceeding)
                .build();

        assertThat(proceedingsChecker.isStarted(caseData)).isFalse();
    }

    @Test
    void shouldReturnTrueWhenProceedingsNotEmpty() {
        final Proceeding proceeding = Proceeding.builder()
                .onGoingProceeding("No")
                .build();
        final CaseData caseData = CaseData.builder()
                .proceeding(proceeding)
                .build();

        assertThat(proceedingsChecker.isStarted(caseData)).isTrue();
    }

    private static Stream<Arguments> emptyProceedings() {
        return Stream.of(
                Proceeding.builder()
                        .build(),
                Proceeding.builder()
                        .onGoingProceeding("")
                        .build())
                .map(Arguments::of);
    }
}
