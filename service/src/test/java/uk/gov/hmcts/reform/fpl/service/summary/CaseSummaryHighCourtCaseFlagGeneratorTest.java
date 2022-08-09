package uk.gov.hmcts.reform.fpl.service.summary;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;

import static org.assertj.core.api.Assertions.assertThat;

class CaseSummaryHighCourtCaseFlagGeneratorTest {

    private final CaseSummaryHighCourtCaseFlagGenerator  underTest = new CaseSummaryHighCourtCaseFlagGenerator();

    @Test
    void generateWhenCourtIsNull() {
        CaseData caseData = CaseData.builder().court(null).build();
        SyntheticCaseSummary caseSummary = underTest.generate(caseData);
        assertThat(caseSummary).isEqualTo(SyntheticCaseSummary.builder()
            .caseSummaryHighCourtCase("No")
            .caseSummaryLAHighCourtCase("No")
            .build()
        );
    }

    @Test
    void generateWhenCourtCodeIsEqRcjHighCourtCode() {
        CaseData caseData = CaseData.builder().court(Court.builder().code("100").build()).build();
        SyntheticCaseSummary caseSummary = underTest.generate(caseData);
        assertThat(caseSummary).isEqualTo(SyntheticCaseSummary.builder()
            .caseSummaryHighCourtCase("Yes")
            .caseSummaryLAHighCourtCase("Yes")
            .build()
        );
    }

    @Test
    void generateWhenCourtCodeIsOrdinaryCourtCode() {
        CaseData caseData = CaseData.builder().court(Court.builder().code("117").build()).build();
        SyntheticCaseSummary caseSummary = underTest.generate(caseData);
        assertThat(caseSummary).isEqualTo(SyntheticCaseSummary.builder()
            .caseSummaryHighCourtCase("No")
            .caseSummaryLAHighCourtCase("No")
            .build()
        );
    }
}
