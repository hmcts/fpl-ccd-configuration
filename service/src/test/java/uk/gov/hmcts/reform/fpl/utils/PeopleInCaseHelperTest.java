package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.DEPUTY_DISTRICT_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.DISTRICT_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.OTHER;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getAllocatedJudgeName;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getAllocatedJudgeTitle;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

class PeopleInCaseHelperTest {
    @Test
    void shouldReturnFirstRespondentSurnameWhenFirstRespondentWithNamePresent() {

        String respondentName = getFirstRespondentLastName(createRespondents());
        assertThat(respondentName).isEqualTo("Jones");
    }

    @Test
    void shouldReturnEmptyStringWhenNoRespondents() {

        String respondentName = getFirstRespondentLastName(null);
        assertThat(respondentName).isEmpty();
    }

    @Test
    void shouldReturnEmptyStringWhenRespondentWithNoPartyPresent() {

        String respondentName = getFirstRespondentLastName(wrapElements(Respondent.builder().build()));
        assertThat(respondentName).isEmpty();
    }

    @Test
    void shouldReturnEmptyStringWhenRespondentWithNoNamePresent() {
        List<Element<Respondent>> respondents = wrapElements(Respondent.builder()
            .party(RespondentParty.builder().build())
            .build());

        String respondentName = getFirstRespondentLastName(respondents);
        assertThat(respondentName).isEmpty();
    }

    @Test
    void shouldReturnAllocatedJudgeOtherTitleWhenOtherIsSelected() {
        CaseData data = buildCaseDataWithAllocatedJudge(OTHER);

        String title = getAllocatedJudgeTitle(data);
        assertThat(title).isEqualTo("Other title");
    }

    @Test
    void shouldReturnAllocatedJudgeTitleWhenOtherIsNotSelected() {
        CaseData data = buildCaseDataWithAllocatedJudge(DISTRICT_JUDGE);
        String title = getAllocatedJudgeTitle(data);

        assertThat(title).isEqualTo(DISTRICT_JUDGE.getLabel());
    }

    @Test
    void shouldReturnAllocatedJudgeFullNameWhenMagistratesSelected() {
        CaseData data = buildCaseDataWithAllocatedJudge(MAGISTRATES);
        String judgeFullName = getAllocatedJudgeName(data);

        assertThat(judgeFullName).isEqualTo("Judge Full Name");
    }

    @Test
    void shouldReturnAllocatedJudgeLastName() {
        CaseData data = buildCaseDataWithAllocatedJudge(DEPUTY_DISTRICT_JUDGE);
        String judgeLastName = getAllocatedJudgeName(data);

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
