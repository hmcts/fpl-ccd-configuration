package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.buildAllocatedJudgeLabel;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getHearingJudge;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getLegalAdvisorName;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getSelectedJudge;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.prepareJudgeFields;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.removeAllocatedJudgeProperties;

class JudgeAndLegalAdvisorHelperTest {

    @Test
    void shouldReturnEmptyLegalAdvisorNameWhenJudgeAndLegalAdvisorIsNull() {
        String legalAdvisorName = getLegalAdvisorName(null);

        assertThat(legalAdvisorName).isEmpty();
    }

    @Test
    void shouldReturnEmptyJudgeNameWhenJudgeAndLegalAdvisorIsNull() {
        String judgeTitleAndName = formatJudgeTitleAndName(null);

        assertThat(judgeTitleAndName).isEmpty();
    }

    @Test
    void shouldReturnEmptyJudgeNameWhenJudgeTitleIsNotProvided() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeTitle(null)
            .legalAdvisorName("Freddie")
            .build();

        String judgeTitleAndName = formatJudgeTitleAndName(judgeAndLegalAdvisor);

        assertThat(judgeTitleAndName).isEmpty();
    }

    @Test
    void shouldReturnEmptyJudgeNameWhenJudgeTitleIsProvidedAndJudgeNameIsNotProvided() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeTitle(HIS_HONOUR_JUDGE)
            .judgeLastName(null)
            .build();

        String judgeTitleAndName = formatJudgeTitleAndName(judgeAndLegalAdvisor);

        assertThat(judgeTitleAndName).isEmpty();
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
    void shouldExtractOtherTitleDescriptionWhenOtherTitleSelected() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeTitle(OTHER)
            .otherTitle("His Excellency")
            .judgeLastName("John Doe")
            .build();

        String magistrateName = JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName(judgeAndLegalAdvisor);

        assertThat(magistrateName).isEqualTo("His Excellency John Doe");
    }

    @Test
    void shouldReturnAllocatedJudgeWhenUseAllocatedJudgeSelected() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = buildJudgeAndLegalAdvisor(YES);

        Judge allocatedJudge = buildJudge();

        JudgeAndLegalAdvisor expectedJudge = getSelectedJudge(judgeAndLegalAdvisor, allocatedJudge);

        assertThat(expectedJudge.getJudgeTitle()).isEqualTo(HIS_HONOUR_JUDGE);
        assertThat(expectedJudge.getJudgeLastName()).isEqualTo("Dread");
        assertThat(expectedJudge.getLegalAdvisorName()).isEqualTo("John Papa");
    }

    @Test
    void shouldReturnInputtedJudgeWhenUseAllocatedJudgeNotSelected() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = buildJudgeAndLegalAdvisor(NO);

        Judge allocatedJudge = buildJudge();

        JudgeAndLegalAdvisor expectedJudge = getSelectedJudge(judgeAndLegalAdvisor, allocatedJudge);

        assertThat(expectedJudge.getJudgeTitle()).isEqualTo(MAGISTRATES);
        assertThat(expectedJudge.getJudgeLastName()).isEqualTo("Stevenson");
        assertThat(expectedJudge.getLegalAdvisorName()).isEqualTo("John Papa");
    }

    @Test
    void shouldBuildAllocatedJudgeLabelWhenAllocatedJudgeIncludesTitleAndLastName() {
        Judge allocatedJudge = buildJudge();

        String label = buildAllocatedJudgeLabel(allocatedJudge);

        assertThat(label).isEqualTo("Case assigned to: His Honour Judge Dread");
    }

    @Test
    void shouldBuildAllocatedJudgeLabelWhenAllocatedJudgeIncludesOtherTitleAndFullName() {
        Judge allocatedJudge = Judge.builder()
            .judgeTitle(OTHER)
            .otherTitle("Mr")
            .judgeLastName("Watson")
            .judgeEmailAddress("watson@example.com")
            .build();

        String label = buildAllocatedJudgeLabel(allocatedJudge);

        assertThat(label).isEqualTo("Case assigned to: Mr Watson");
    }

    @Test
    void shouldRemoveAllocatedJudgeProperties() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .allocatedJudgeLabel("Case assigned to: xxx")
            .useAllocatedJudge("Yes")
            .build();

        removeAllocatedJudgeProperties(judgeAndLegalAdvisor);

        assertThat(judgeAndLegalAdvisor.getAllocatedJudgeLabel()).isNull();
        assertThat(judgeAndLegalAdvisor.getUseAllocatedJudge()).isNull();
    }

    @Test
    void shouldPopulateUseAllocatedJudgeWithYesAndResetJudgeFieldsWhenJudgesAreEqual() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeTitle(OTHER)
            .otherTitle("Mr")
            .judgeLastName("Watson")
            .build();

        Judge allocatedJudge = Judge.builder()
            .judgeTitle(OTHER)
            .otherTitle("Mr")
            .judgeLastName("Watson")
            .build();

        judgeAndLegalAdvisor = prepareJudgeFields(judgeAndLegalAdvisor, allocatedJudge);
        assertThat(judgeAndLegalAdvisor.getUseAllocatedJudge()).isEqualTo("Yes");
        assertThat(judgeAndLegalAdvisor.getJudgeFullName()).isNull();
        assertThat(judgeAndLegalAdvisor.getJudgeLastName()).isNull();
        assertThat(judgeAndLegalAdvisor.getJudgeTitle()).isNull();
    }

    @Test
    void shouldPopulateUseAllocatedJudgeWithNoWhenJudgesAreNotEqual() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeTitle(HIS_HONOUR_JUDGE)
            .judgeLastName("Hastings")
            .build();

        Judge allocatedJudge = Judge.builder()
            .judgeTitle(OTHER)
            .otherTitle("Mr")
            .judgeLastName("Watson")
            .build();

        judgeAndLegalAdvisor = prepareJudgeFields(judgeAndLegalAdvisor, allocatedJudge);
        assertThat(judgeAndLegalAdvisor.getUseAllocatedJudge()).isEqualTo("No");
    }

    @Test
    void shouldReturnHearingJudgeWhenAllocatedJudgeNotBeingUsed() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeTitle(HIS_HONOUR_JUDGE)
            .judgeLastName("Hastings")
            .useAllocatedJudge("No")
            .build();

        assertThat(getHearingJudge(judgeAndLegalAdvisor)).isEqualTo("His Honour Judge Hastings");
    }

    @Test
    void shouldReturnNullWhenAllocatedJudgeIsBeingUsed() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeTitle(HIS_HONOUR_JUDGE)
            .judgeLastName("Hastings")
            .useAllocatedJudge("Yes")
            .build();

        assertThat(getHearingJudge(judgeAndLegalAdvisor)).isNull();
    }

    private JudgeAndLegalAdvisor buildJudgeAndLegalAdvisor(YesNo useAllocatedJudge) {
        return JudgeAndLegalAdvisor.builder()
            .judgeTitle(MAGISTRATES)
            .judgeLastName("Stevenson")
            .legalAdvisorName("John Papa")
            .useAllocatedJudge(useAllocatedJudge.getValue())
            .allocatedJudgeLabel("Case assigned to: His Honour Judge Dread")
            .build();
    }

    private Judge buildJudge() {
        return Judge.builder()
            .judgeTitle(HIS_HONOUR_JUDGE)
            .judgeLastName("Dread")
            .judgeEmailAddress("dread@example.com")
            .build();
    }
}

