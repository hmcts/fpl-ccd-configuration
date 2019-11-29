package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.OTHER;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getLegalAdvisorName;

class JudgeAndLegalAdvisorHelperTest {

    @Test
    void shouldReturnEmptyLegalAdvisorNameWhenJudgeAndLegalAdvisorIsNull() {
        String legalAdvisorName = getLegalAdvisorName(null);

        assertThat(legalAdvisorName).isEqualTo("");
    }

    @Test
    void shouldReturnEmptyJudgeNameWhenJudgeAndLegalAdvisorIsNull() {
        String judgeTitleAndName = formatJudgeTitleAndName(null);

        assertThat(judgeTitleAndName).isEqualTo("");
    }

    @Test
    void shouldReturnEmptyJudgeNameWhenJudgeTitleIsNotProvided() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeTitle(null)
            .legalAdvisorName("Freddie")
            .build();

        String judgeTitleAndName = formatJudgeTitleAndName(judgeAndLegalAdvisor);

        assertThat(judgeTitleAndName).isEqualTo("");
    }

    @Test
    void shouldReturnEmptyJudgeNameWhenJudgeTitleIsProvidedAndJudgeNameIsNotProvided() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeTitle(HIS_HONOUR_JUDGE)
            .judgeLastName(null)
            .build();

        String judgeTitleAndName = formatJudgeTitleAndName(judgeAndLegalAdvisor);

        assertThat(judgeTitleAndName).isEqualTo("");
    }

    @Test
    void shouldReturnProperlyFormattedJudgeNameWhenTitleAndNameAreProvided() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeTitle(HIS_HONOUR_JUDGE)
            .judgeLastName("Dredd")
            .build();

        String judgeTitleAndName = formatJudgeTitleAndName(judgeAndLegalAdvisor);

        assertThat(judgeTitleAndName).isEqualTo("His Honour Judge Dredd");
    }

    @Test
    void shouldReturnProperlyFormattedMagistrateNameWhenMagistrateIsSelected() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeTitle(MAGISTRATES)
            .judgeFullName("Steve Stevenson")
            .build();

        String magistrateName = formatJudgeTitleAndName(judgeAndLegalAdvisor);

        assertThat(magistrateName).isEqualTo("Steve Stevenson (JP)");
    }

    @Test
    void shouldReturnJusticeOfPeaceWhenMagistrateWithoutNameIsSelected() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeTitle(MAGISTRATES)
            .build();

        String magistrateName = JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName(judgeAndLegalAdvisor);

        assertThat(magistrateName).isEqualTo("Justice of the Peace");
    }

    @Test
    private void shouldExtractOtherTitleDescriptionWhenOtherTitleSelected() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeTitle(OTHER)
            .otherTitle("His Excellency")
            .judgeLastName("John Doe")
            .build();

        String magistrateName = JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName(judgeAndLegalAdvisor);

        assertThat(magistrateName).isEqualTo("His Excellency John Doe");
    }
}

