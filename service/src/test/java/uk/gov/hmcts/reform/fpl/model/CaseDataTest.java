package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;

class CaseDataTest {

    private CaseData.CaseDataBuilder caseDataBuilder;
    private Respondent expectedRespondent;

    @BeforeEach
    void setUp() {
        caseDataBuilder = CaseData.builder();
    }

    @Test
    void shouldReturnTheFirstElementOfTheRespondentListWhenListIsPopulated() {
        List<Element<Respondent>> respondents = createRespondents();
        expectedRespondent = respondents.get(0).getValue();

        caseDataBuilder.respondents1(respondents);

        final Respondent respondent = caseDataBuilder.build().getFirstRespondent();

        assertThat(respondent).isEqualTo(expectedRespondent);
    }

    @Test
    void shouldReturnABlankRespondentWhenTheRespondentListIsNotPopulated() {
        expectedRespondent = Respondent.builder().build();

        final Respondent respondent = caseDataBuilder.build().getFirstRespondent();

        assertThat(respondent).isEqualTo(expectedRespondent);
    }

}
