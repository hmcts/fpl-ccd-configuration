package uk.gov.hmcts.reform.fpl.service.summary;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class CaseSummaryDeadlineGeneratorTest {

    private final CaseSummaryDeadlineGenerator underTest = new CaseSummaryDeadlineGenerator();

    @Test
    void testGenerateEmptySumbittedDate() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder().build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.emptySummary());
    }

    @Test
    void testGenerate() {

        SyntheticCaseSummary actual = underTest.generate(CaseData.builder()
            .dateSubmitted(LocalDate.of(2012,1,1))
            .build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.builder()
            .caseSummaryDateOfIssue(LocalDate.of(2012,1,1))
            .deadline26week(LocalDate.of(2012,7,1))
            .build());
    }
}
