package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.IsAddressKnowType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.Element;

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
    void shouldReturnEmptyErrorsAndNonCompletedStateForOptionalEvent(List<Element<Other>> others) {
        final CaseData caseData = CaseData.builder()
                .othersV2(others)
                .build();

        final List<String> errors = othersChecker.validate(caseData);
        final boolean isCompleted = othersChecker.isCompleted(caseData);

        assertThat(errors).isEmpty();
        assertThat(isCompleted).isFalse();
    }

    private static Stream<Arguments> others() {
        return Stream.of(
                List.of(),
                wrapElements(Other.builder().build(), Other.builder().build()),
                wrapElements(Other.builder()
                        .firstName("Test")
                        .build()),
                wrapElements(Other.builder()
                    .firstName("Test")
                    .lastName("last")
                    .build()),
                wrapElements(Other.builder()
                    .firstName("Test")
                    .lastName("last")
                    .addressKnowV2(IsAddressKnowType.NO)
                    .build()),
                wrapElements(Other.builder()
                    .addressKnowV2(IsAddressKnowType.YES)
                    .build())
            )
                .map(Arguments::of);
    }
}
