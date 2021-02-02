package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Proceeding;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ProceedingsCheckerTest {

    @InjectMocks
    private ProceedingsChecker proceedingsChecker;

    @ParameterizedTest
    @NullSource
    @MethodSource("proceedings")
    void shouldReturnEmptyErrorsAndNonCompletedStateForOptionalEvent(Proceeding proceeding) {
        final CaseData caseData = CaseData.builder()
                .proceeding(proceeding)
                .build();

        final List<String> errors = proceedingsChecker.validate(caseData);
        final boolean isCompleted = proceedingsChecker.isCompleted(caseData);

        assertThat(errors).isEmpty();
        assertThat(isCompleted).isFalse();
    }

    private static Stream<Arguments> proceedings() {
        return Stream.of(
                Proceeding.builder()
                        .build(),
                Proceeding.builder()
                        .onGoingProceeding("")
                        .additionalProceedings(emptyList())
                        .build())
                .map(Arguments::of);
    }
}
