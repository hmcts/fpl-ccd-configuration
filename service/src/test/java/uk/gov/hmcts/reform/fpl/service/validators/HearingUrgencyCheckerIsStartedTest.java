package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.hearing.HearingUrgencyType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Hearing;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@ExtendWith(MockitoExtension.class)
class HearingUrgencyCheckerIsStartedTest {

    @InjectMocks
    private HearingUrgencyChecker hearingUrgencyValidator;

    @ParameterizedTest
    @NullSource
    @MethodSource("emptyHearingUrgency")
    void shouldReturnFalseWhenEmptyHearingUrgency(Hearing hearing) {
        final CaseData caseData = CaseData.builder()
                .hearing(hearing)
                .build();

        assertThat(hearingUrgencyValidator.isStarted(caseData)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("nonEmptyHearingUrgency")
    void shouldReturnTrueWhenNonEmptyHearingUrgency(Hearing hearing) {
        final CaseData caseData = CaseData.builder()
                .hearing(hearing)
                .build();

        assertThat(hearingUrgencyValidator.isStarted(caseData)).isTrue();
    }

    private static Stream<Arguments> nonEmptyHearingUrgency() {
        return Stream.of(
                Hearing.builder().hearingUrgencyType(HearingUrgencyType.STANDARD),
                Hearing.builder().respondentsAware(YES).build())
                .map(Arguments::of);
    }

    private static Stream<Arguments> emptyHearingUrgency() {
        return Stream.of(
                Hearing.builder().build())
                .map(Arguments::of);
    }
}
