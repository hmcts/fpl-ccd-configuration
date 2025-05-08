package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class OthersCheckerTest {

    @InjectMocks
    private OthersChecker othersChecker;

    @ParameterizedTest
    @NullSource
    @MethodSource("others")
    void shouldReturnEmptyErrorsAndNonCompletedStateForOptionalEvent(Others others) {
        final CaseData caseData = CaseData.builder()
                .others(others)
                .build();

        final List<String> errors = othersChecker.validate(caseData);
        final boolean isCompleted = othersChecker.isCompleted(caseData);

        assertThat(errors).isEmpty();
        assertThat(isCompleted).isFalse();
    }

    private static Stream<Arguments> others() {
        return Stream.of(
                Others.builder().build(),
                Others.builder()
                        .firstOther(Other.builder().build())
                        .additionalOthers(wrapElements(Other.builder().build())).build(),
                Others.builder()
                        .firstOther(Other.builder()
                                .name("Test")
                                .build())
                        .additionalOthers(wrapElements(Other.builder()
                                .gender("Male")
                                .build())).build())
                .map(Arguments::of);
    }
}
