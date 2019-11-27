package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;

class JudgeAndLegalAdvisorHelperTest {

    @Test
    void shouldReturnEmptyLegalAdvisorNameWhenJudgeAndLegalAdvisorIsNull() {
        String legalAdvisorName = JudgeAndLegalAdvisorHelper.getLegalAdvisorName(null);

        assertThat(legalAdvisorName).isEqualTo("");
    }

    @Test
    void shouldReturnEmptyJudgeNameWhenJudgeAndLegalAdvisorIsNull() {
        String judgeTitleAndName = JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName(null);

        assertThat(judgeTitleAndName).isEqualTo("");
    }

    @Test
    void shouldReturnEmptyJudgeNameWhenJudgeTitleIsNotProvided() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeTitle(null)
            .legalAdvisorName("Freddie")
            .build();

        String judgeTitleAndName = JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName(judgeAndLegalAdvisor);

        assertThat(judgeTitleAndName).isEqualTo("");
    }

    @Test
    void shouldReturnBlankPlaceholderWhenNoJudgeEnteredForDraftSDO() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeTitle(null)
            .build();

        String judgeTitleAndName = JudgeAndLegalAdvisorHelper.formatJudgeTitleAndNameForDraftSDO(judgeAndLegalAdvisor,
            "BLANK");

        assertThat(judgeTitleAndName).isEqualTo("BLANK");
    }

    @Test
    void shouldReturnProperlyFormattedJudgeNameWhenTitleAndNameAreProvided() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeTitle(HIS_HONOUR_JUDGE)
            .judgeLastName("Dredd")
            .build();

        String judgeTitleAndName = JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName(judgeAndLegalAdvisor);

        assertThat(judgeTitleAndName).isEqualTo("His Honour Judge Dredd");
    }

    @Test
    void shouldReturnProperlyFormattedMagistrateNameWhenMagistrateIsSelected() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeTitle(MAGISTRATES)
            .judgeFullName("Steve Stevenson")
            .build();

        String magistrateName = JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName(judgeAndLegalAdvisor);

        assertThat(magistrateName).isEqualTo("Steve Stevenson (JP)");
    }
}
