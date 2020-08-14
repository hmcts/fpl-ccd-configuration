package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FactorsParenting;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class FactorsAffectingParentingCheckerTest {

    @InjectMocks
    private FactorsAffectingParentingChecker factorsAffectingParentingChecker;

    @ParameterizedTest
    @NullSource
    @MethodSource("factorsAffectingParenting")
    void shouldReturnEmptyErrorsAndNonCompletedStateForOptionalEvent(FactorsParenting factorsAffectingParenting) {
        final CaseData caseData = CaseData.builder()
                .factorsParenting(factorsAffectingParenting)
                .build();

        final List<String> errors = factorsAffectingParentingChecker.validate(caseData);
        final boolean isCompleted = factorsAffectingParentingChecker.isCompleted(caseData);

        assertThat(errors).isEmpty();
        assertThat(isCompleted).isFalse();
    }

    private static Stream<Arguments> factorsAffectingParenting() {
        return Stream.of(
                FactorsParenting.builder().build(),
                FactorsParenting.builder()
                        .alcoholDrugAbuse("")
                        .domesticViolence("")
                        .anythingElse("")
                        .build(),
                FactorsParenting.builder()
                        .alcoholDrugAbuse("Yes")
                        .domesticViolence("No")
                        .anythingElse("Yes")
                        .build())
                .map(Arguments::of);
    }
}
