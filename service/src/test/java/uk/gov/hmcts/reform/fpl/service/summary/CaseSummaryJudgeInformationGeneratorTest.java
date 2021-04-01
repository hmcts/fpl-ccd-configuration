package uk.gov.hmcts.reform.fpl.service.summary;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;
import uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CaseSummaryJudgeInformationGeneratorTest {

    private static final String FORMATTED_JUDGE = "formatted judge";
    private static final String JUDGE_EMAIL = "judgeEmail";
    private final CaseSummaryJudgeInformationGenerator underTest = new CaseSummaryJudgeInformationGenerator();
    private static final Judge ALLOCATED_JUDGE = mock(Judge.class);
    private static final JudgeAndLegalAdvisor JUDGE_AND_LEGAL_ADVISOR = mock(JudgeAndLegalAdvisor.class);

    @Test
    void testNoAllocatedJudge() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder().build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.emptySummary());
    }

    @Test
    void testAllocatedJudgeWithNoData() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder()
            .allocatedJudge(Judge.builder().build())
            .build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.emptySummary());
    }

    @Test
    void testAllocatedJudgeWithJudgeInformation() {
        try (
            MockedStatic<JudgeAndLegalAdvisorHelper> mockStatic =
                Mockito.mockStatic(JudgeAndLegalAdvisorHelper.class)) {

            when(ALLOCATED_JUDGE.toJudgeAndLegalAdvisor()).thenReturn(JUDGE_AND_LEGAL_ADVISOR);
            mockStatic.when(() -> JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName(
                JUDGE_AND_LEGAL_ADVISOR))
                .thenReturn(FORMATTED_JUDGE);

            SyntheticCaseSummary actual = underTest.generate(CaseData.builder()
                .allocatedJudge(ALLOCATED_JUDGE)
                .build());

            assertThat(actual).isEqualTo(SyntheticCaseSummary.builder()
                .caseSummaryAllocatedJudgeName(FORMATTED_JUDGE)
                .build());
        }
    }

    @Test
    void testAllocatedJudgeWithJudgeEmail() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder()
            .allocatedJudge(Judge.builder()
                .judgeEmailAddress(JUDGE_EMAIL)
                .build())
            .build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.builder()
            .caseSummaryAllocatedJudgeEmail(JUDGE_EMAIL)
            .build());
    }
}
