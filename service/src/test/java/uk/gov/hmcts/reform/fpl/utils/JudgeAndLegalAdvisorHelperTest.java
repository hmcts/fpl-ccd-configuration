package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import static org.assertj.core.api.Assertions.assertThat;

class JudgeAndLegalAdvisorHelperTest {

    @Test
    void shouldHaveEmptyLegalAdvisorNameWhenJudgeAndLegalAdvisorNull() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = null;

        String legalAdvisorName = JudgeAndLegalAdvisorHelper.getLegalAdvisorName(judgeAndLegalAdvisor);

        assertThat(legalAdvisorName).isEqualTo("");
    }

    @Test
    void shouldHaveEmptyJudgeNameWhenJudgeAndLegalAdvisorNull() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = null;

        String judgeTitleAndName = JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName(judgeAndLegalAdvisor);

        assertThat(judgeTitleAndName).isEqualTo("");
    }

    @Test
    void shouldHaveEmptyJudgeNameWhenJudgeTitleAndNameNotProvided() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .legalAdvisorName("Freddie")
            .build();

        String judgeTitleAndName = JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName(judgeAndLegalAdvisor);

        assertThat(judgeTitleAndName).isEqualTo("");
    }

    @Test
    void shouldHaveProperlyFormattedJudgeNameWhenTitleAndNameProvided() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeTitle(JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE)
            .judgeLastName("Dredd")
            .build();

        String judgeTitleAndName = JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName(judgeAndLegalAdvisor);

        assertThat(judgeTitleAndName).isEqualTo("His Honour Judge Dredd");
    }

    @Test
    void shouldHaveProperlyFormattedMagistrateNameWhenWhenMagistrateSelected() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeTitle(JudgeOrMagistrateTitle.MAGISTRATES)
            .judgeFullName("Steve Stevenson")
            .build();

        String result = JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName(judgeAndLegalAdvisor);

        assertThat(result).isEqualTo("Steve Stevenson (JP)");
    }
}
