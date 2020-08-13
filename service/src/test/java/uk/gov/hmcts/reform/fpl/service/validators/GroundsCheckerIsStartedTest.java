package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Grounds;
import uk.gov.hmcts.reform.fpl.model.GroundsForEPO;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GroundsCheckerIsStartedTest {

    @InjectMocks
    private GroundsChecker groundsChecker;

    @ParameterizedTest
    @MethodSource("emptyGrounds")
    void shouldReturnFalseWhenEmptyGrounds(Grounds grounds, GroundsForEPO groundsForEPO) {
        final CaseData caseData = CaseData.builder()
                .grounds(grounds)
                .groundsForEPO(groundsForEPO)
                .build();

        assertThat(groundsChecker.isStarted(caseData)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("nonEmptyGrounds")
    void shouldReturnTrueWhenGroundsProvided(Grounds grounds) {
        final CaseData caseData = CaseData.builder()
                .grounds(grounds)
                .build();

        assertThat(groundsChecker.isStarted(caseData)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("nonEmptyGroundsForEPO")
    void shouldReturnTrueWhenGroundsForEPOProvided(GroundsForEPO groundsForEPO) {
        final CaseData caseData = CaseData.builder()
                .groundsForEPO(groundsForEPO)
                .build();

        assertThat(groundsChecker.isStarted(caseData)).isTrue();
    }

    private static Stream<Arguments> nonEmptyGrounds() {
        return Stream.of(
                Grounds.builder().thresholdReason(List.of(("Test"))).build(),
                Grounds.builder().thresholdDetails("Test").build())
                .map(Arguments::of);
    }

    private static Stream<Arguments> nonEmptyGroundsForEPO() {
        return Stream.of(
                GroundsForEPO.builder().reason(List.of("Test")).build())
                .map(Arguments::of);
    }

    private static Stream<Arguments> emptyGrounds() {
        return Stream.of(
                Arguments.of(
                        Grounds.builder().build(),
                        GroundsForEPO.builder().build()),
                Arguments.of(
                        Grounds.builder()
                                .thresholdDetails("")
                                .thresholdReason(emptyList())
                                .build(),
                        GroundsForEPO.builder()
                                .reason(emptyList())
                                .build()));
    }
}
