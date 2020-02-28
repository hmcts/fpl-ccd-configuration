package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;

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

    private Judge buildAllocatedJudge(JudgeOrMagistrateTitle title) {
        return Judge.builder()
            .judgeTitle(title)
            .otherTitle("Other title")
            .judgeFullName("Judge Full Name")
            .judgeLastName("Judge Last Name")
            .build();
    }
}
