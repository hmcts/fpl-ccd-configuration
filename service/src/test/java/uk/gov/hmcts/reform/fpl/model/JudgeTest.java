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
        CaseData data = buildCaseDataWithAllocatedJudge(OTHER);
        String title = data.getAllocatedJudge().getAllocatedJudgeTitle();

        assertThat(title).isEqualTo("Other title");
    }

    @Test
    void shouldReturnAllocatedJudgeTitleWhenOtherIsNotSelected() {
        CaseData data = buildCaseDataWithAllocatedJudge(DISTRICT_JUDGE);
        String title = data.getAllocatedJudge().getAllocatedJudgeTitle();

        assertThat(title).isEqualTo(DISTRICT_JUDGE.getLabel());
    }

    @Test
    void shouldReturnAllocatedJudgeFullNameWhenMagistratesSelected() {
        CaseData data = buildCaseDataWithAllocatedJudge(MAGISTRATES);
        String judgeFullName = data.getAllocatedJudge().getAllocatedJudgeName();

        assertThat(judgeFullName).isEqualTo("Judge Full Name");
    }

    @Test
    void shouldReturnAllocatedJudgeLastNameWhenMagistratesIsNotSelected() {
        CaseData data = buildCaseDataWithAllocatedJudge(DEPUTY_DISTRICT_JUDGE);
        String judgeLastName = data.getAllocatedJudge().getAllocatedJudgeName();

        assertThat(judgeLastName).isEqualTo("Judge Last Name");
    }

    private CaseData buildCaseDataWithAllocatedJudge(JudgeOrMagistrateTitle title) {
        return CaseData.builder().allocatedJudge(Judge.builder()
            .judgeTitle(title)
            .otherTitle("Other title")
            .judgeFullName("Judge Full Name")
            .judgeLastName("Judge Last Name")
            .build())
            .build();
    }
}
