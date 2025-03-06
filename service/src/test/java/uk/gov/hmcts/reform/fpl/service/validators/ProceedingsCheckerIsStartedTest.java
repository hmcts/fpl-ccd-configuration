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
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class ProceedingsCheckerIsStartedTest {

    @InjectMocks
    private ProceedingsChecker proceedingsChecker;

    @ParameterizedTest
    @NullSource
    @MethodSource("emptyProceedings")
    void shouldReturnFalseWhenEmptyProceedings(List<Element<Proceeding>> proceedings) {
        final CaseData caseData = CaseData.builder()
                .proceedings(proceedings)
                .build();

        assertThat(proceedingsChecker.isStarted(caseData)).isFalse();
    }

    @Test
    void shouldReturnTrueWhenProceedingsNotEmpty() {
        final CaseData caseData = CaseData.builder()
                .proceedings(wrapElements(Proceeding.builder().build()))
                .build();

        assertThat(proceedingsChecker.isStarted(caseData)).isTrue();
    }

    private static Stream<Arguments> emptyProceedings() {
        return Stream.of(
                null,
                List.of())
                .map(Arguments::of);
    }
}
