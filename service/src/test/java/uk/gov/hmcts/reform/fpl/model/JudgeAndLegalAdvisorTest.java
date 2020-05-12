package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testJudge;

class JudgeAndLegalAdvisorTest {
    @Test
    void shouldReturnExpectedJudgeAndLegalAdvisor() {
        assertThat(JudgeAndLegalAdvisor.from(testJudge()))
            .isEqualTo(JudgeAndLegalAdvisor.builder()
                .judgeTitle(MAGISTRATES)
                .judgeLastName("Stark")
                .judgeFullName("Brandon Stark")
                .build());
    }

    @Test
    void shouldReturnNullFieldsForJudgeAndLegalAdvisorWhenJudgeNull() {
        assertThat(JudgeAndLegalAdvisor.from(null))
            .isEqualToComparingFieldByField(JudgeAndLegalAdvisor.builder().build());
    }

    @Test
    void shouldResetJudgePropertiesWhilePersistingLegalAdvisorName() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeTitle(MAGISTRATES)
            .judgeFullName("Davidson")
            .legalAdvisorName("Holmes")
            .build();

        judgeAndLegalAdvisor = judgeAndLegalAdvisor.resetJudgeProperties();

        assertThat(judgeAndLegalAdvisor.getLegalAdvisorName()).isEqualTo("Holmes");
        assertThat(judgeAndLegalAdvisor.getUseAllocatedJudge()).isEqualTo("Yes");
        assertThat(judgeAndLegalAdvisor.getJudgeTitle()).isNull();
        assertThat(judgeAndLegalAdvisor.getJudgeFullName()).isNull();
    }
}
