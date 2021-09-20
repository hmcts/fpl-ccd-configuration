package uk.gov.hmcts.reform.fpl.service.summary;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;

import static org.assertj.core.api.Assertions.assertThat;

class CaseSummaryWelshFlagGeneratorTest {

    private final CaseSummaryWelshFlagGenerator underTest = new CaseSummaryWelshFlagGenerator();


    @ParameterizedTest
    @NullAndEmptySource
    void generateWhenLanguageRequirementIsNullOrEmpty(String requirement) {
        CaseData caseData = CaseData.builder().languageRequirement(requirement).build();

        SyntheticCaseSummary caseSummary = underTest.generate(caseData);

        assertThat(caseSummary).isEqualTo(SyntheticCaseSummary.builder()
            .caseSummaryLALanguageRequirement("No")
            .caseSummaryLanguageRequirement("No")
            .build()
        );
    }

    @Test
    void generateWhenLanguageRequirementIsNo() {
        CaseData caseData = CaseData.builder().languageRequirement("No").build();

        SyntheticCaseSummary caseSummary = underTest.generate(caseData);

        assertThat(caseSummary).isEqualTo(SyntheticCaseSummary.builder()
            .caseSummaryLALanguageRequirement("No")
            .caseSummaryLanguageRequirement("No")
            .build()
        );
    }

    @Test
    void generateWhenLanguageRequirementIsYes() {
        CaseData caseData = CaseData.builder().languageRequirement("Yes").build();

        SyntheticCaseSummary caseSummary = underTest.generate(caseData);

        assertThat(caseSummary).isEqualTo(SyntheticCaseSummary.builder()
            .caseSummaryLALanguageRequirement("Yes")
            .caseSummaryLanguageRequirement("Yes")
            .build()
        );
    }
}
