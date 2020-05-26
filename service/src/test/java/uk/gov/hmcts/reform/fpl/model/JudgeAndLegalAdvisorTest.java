package uk.gov.hmcts.reform.fpl.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.DISTRICT_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
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

        judgeAndLegalAdvisor = judgeAndLegalAdvisor.reset();

        assertThat(judgeAndLegalAdvisor.getLegalAdvisorName()).isEqualTo("Holmes");
        assertThat(judgeAndLegalAdvisor.getUseAllocatedJudge()).isEqualTo(YES.getValue());
        assertThat(judgeAndLegalAdvisor.getJudgeTitle()).isNull();
        assertThat(judgeAndLegalAdvisor.getJudgeFullName()).isNull();
    }

    @Test
    void shouldReturnAllocatedJudgeOtherTitleWhenOtherIsSelected() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeTitle(OTHER)
            .otherTitle("Other title")
            .build();

        String title = judgeAndLegalAdvisor.getJudgeOrMagistrateTitle();

        Assertions.assertThat(title).isEqualTo("Other title");
    }

    @Test
    void shouldReturnAllocatedJudgeTitleWhenOtherIsNotSelected() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeTitle(DISTRICT_JUDGE)
            .build();

        String title = judgeAndLegalAdvisor.getJudgeOrMagistrateTitle();

        Assertions.assertThat(title).isEqualTo(DISTRICT_JUDGE.getLabel());
    }

    @Test
    void shouldReturnAllocatedJudgeFullNameWhenMagistratesSelected() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeTitle(MAGISTRATES)
            .judgeFullName("Judge Full Name")
            .build();

        String fullName = judgeAndLegalAdvisor.getJudgeName();

        Assertions.assertThat(fullName).isEqualTo("Judge Full Name");
    }

    @Test
    void shouldReturnAllocatedJudgeLastNameWhenMagistratesIsNotSelected() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeTitle(HER_HONOUR_JUDGE)
            .judgeLastName("Judge Last Name")
            .build();

        String lastName = judgeAndLegalAdvisor.getJudgeName();

        Assertions.assertThat(lastName).isEqualTo("Judge Last Name");
    }
}
