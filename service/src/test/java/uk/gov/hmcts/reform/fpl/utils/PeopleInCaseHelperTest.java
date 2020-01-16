package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
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
}
