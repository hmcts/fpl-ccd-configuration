package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import static org.assertj.core.api.Assertions.assertThat;

public class JudgeAndLegalAdvisorHelperTest {
    @Test
    public void givenEmptyJudgeAndLegalAdvisorGetLegalAdvisorNameShouldReturnEmptyString() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = null;

        String result = JudgeAndLegalAdvisorHelper.getLegalAdvisorName(judgeAndLegalAdvisor);

        assertThat(result).isEqualTo("");
    }

    @Test
    public void givenEmptyJudgeAndLegalAdvisorFormatJudgeTitleAndNameShouldReturnEmptyString() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = null;

        String result = JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName(judgeAndLegalAdvisor);

        assertThat(result).isEqualTo("");
    }

    @Test
    public void givenNoJudgeTitleFormatJudgeTitleAndNameShouldReturnEmptyString() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .legalAdvisorName("Freddie")
            .build();

        String result = JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName(judgeAndLegalAdvisor);

        assertThat(result).isEqualTo("");
    }

    @Test
    public void givenJudgeTitleAndNameFormatJudgeTitleAndNameShouldReturnProperlyFormattedTitleAndName() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeTitle(JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE)
            .judgeLastName("Dredd")
            .build();

        String result = JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName(judgeAndLegalAdvisor);

        assertThat(result).isEqualTo("His Honour Judge Dredd");
    }

    @Test
    public void givenMagistrateTitleAndNameFormatJudgeTitleAndNameShouldReturnProperlyFormattedTitleAndName() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeTitle(JudgeOrMagistrateTitle.MAGISTRATES)
            .judgeFullName("Steve Stevenson")
            .build();

        String result = JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName(judgeAndLegalAdvisor);

        assertThat(result).isEqualTo("Steve Stevenson (JP)");
    }

}
