package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CaseSubmissionCheckerIsStartedTest {

    @InjectMocks
    private CaseSubmissionChecker caseSubmissionChecker;

    @Test
    void shouldReturnFalseWhenCaseNotSubmitted() {
        final CaseData caseData = CaseData.builder().build();

        assertThat(caseSubmissionChecker.isStarted(caseData)).isFalse();
    }

    @Test
    void shouldReturnTrueWhenCaseSubmitted() {
        final CaseData caseData = CaseData.builder()
                .dateSubmitted(LocalDate.now())
                .build();

        assertThat(caseSubmissionChecker.isStarted(caseData)).isTrue();
    }

}
