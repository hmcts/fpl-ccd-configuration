package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.DEPUTY_DISTRICT_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.DISTRICT_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.OTHER;

class JudgeTest {

    @Test
    void shouldReturnAllocatedJudgeOtherTitleWhenOtherIsSelected() {
        Judge allocatedJudge = buildAllocatedJudge(OTHER);
        String title = allocatedJudge.getJudgeOrMagistrateTitle();

        assertThat(title).isEqualTo("Other title");
    }

    @Test
    void shouldReturnAllocatedJudgeTitleWhenOtherIsNotSelected() {
        Judge allocatedJudge = buildAllocatedJudge(DISTRICT_JUDGE);
        String title = allocatedJudge.getJudgeOrMagistrateTitle();

        assertThat(title).isEqualTo(DISTRICT_JUDGE.getLabel());
    }

    @Test
    void shouldReturnAllocatedJudgeFullNameWhenMagistratesSelected() {
        Judge allocatedJudge = buildAllocatedJudge(MAGISTRATES);
        String fullName = allocatedJudge.getJudgeName();

        assertThat(fullName).isEqualTo("Judge Full Name");
    }

    @Test
    void shouldReturnAllocatedJudgeLastNameWhenMagistratesIsNotSelected() {
        Judge allocatedJudge = buildAllocatedJudge(DEPUTY_DISTRICT_JUDGE);
        String lastName = allocatedJudge.getJudgeName();

        assertThat(lastName).isEqualTo("Judge Last Name");
    }

    @Test
    void shouldReturnTrueWhenJudgesAreEqual() {
        JudgeOrMagistrateTitle judgeOrMagistrateTitle = MAGISTRATES;

        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeTitle(judgeOrMagistrateTitle)
            .otherTitle("Other title")
            .judgeFullName("Judge Full Name")
            .judgeLastName("Judge Last Name")
            .build();

        Judge allocatedJudge = buildAllocatedJudge(judgeOrMagistrateTitle);
        assertThat(allocatedJudge.equalsJudgeAndLegalAdvisor(judgeAndLegalAdvisor)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenJudgesAreNotEqual() {
        JudgeOrMagistrateTitle judgeOrMagistrateTitle = DEPUTY_DISTRICT_JUDGE;

        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeTitle(judgeOrMagistrateTitle)
            .judgeLastName("Moley")
            .build();

        Judge allocatedJudge = buildAllocatedJudge(judgeOrMagistrateTitle);
        assertThat(allocatedJudge.equalsJudgeAndLegalAdvisor(judgeAndLegalAdvisor)).isFalse();
    }

    private Judge buildAllocatedJudge(JudgeOrMagistrateTitle title) {
        return Judge.builder()
            .judgeTitle(title)
            .otherTitle("Other title")
            .judgeFullName("Judge Full Name")
            .judgeLastName("Judge Last Name")
            .judgeEmailAddress("Judge Email Address")
            .build();
    }
}
