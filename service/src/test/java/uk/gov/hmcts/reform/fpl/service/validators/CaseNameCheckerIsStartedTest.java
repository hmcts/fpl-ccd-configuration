package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CaseNameCheckerIsStartedTest {

    @InjectMocks
    private CaseNameChecker caseNameChecker;

    @Test
    void shouldReturnTrueWhenCaseNameNotEmpty() {
        final CaseData caseData = CaseData.builder()
                .caseName("Test")
                .build();

        assertThat(caseNameChecker.isStarted(caseData)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnFalseWhenCaseNameIsEmpty(String caseName) {
        final CaseData caseData = CaseData.builder()
                .build();

        assertThat(caseNameChecker.isStarted(caseData)).isFalse();
    }
}
