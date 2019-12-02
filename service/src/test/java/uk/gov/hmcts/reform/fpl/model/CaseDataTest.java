package uk.gov.hmcts.reform.fpl.model;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import static org.assertj.core.api.Assertions.assertThat;

class CaseDataTest {

    private CaseData.CaseDataBuilder caseDataBuilder;

    @BeforeEach
    void setUp() {
        caseDataBuilder = CaseData.builder();
    }

    @Test
    void shouldReturnAnEmptyStringWhenRespondentsIsEmpty() {
        final String respondentFullName = caseDataBuilder.build().buildFirstRespondentFullName();

        assertThat(respondentFullName).isEmpty();
    }

    @Test
    void shouldReturnTheFirstAndLastNameAppendedWhenBothAreProvided() {
        caseDataBuilder.respondents1(buildRespondents("Bob", "Ross"));

        final String respondentFullName = caseDataBuilder.build().buildFirstRespondentFullName();

        assertThat(respondentFullName).isEqualTo("Bob Ross");
    }

    @Test
    void shouldOnlyReturnTheFirstNameWhenBothAreProvidedButLastIsEmpty() {
        caseDataBuilder.respondents1(buildRespondents("Bob", ""));

        final String respondentFullName = caseDataBuilder.build().buildFirstRespondentFullName();

        assertThat(respondentFullName).isEqualTo("Bob");
    }

    @Test
    void shouldOnlyReturnTheLastNameWhenBothAreProvidedButFirstIsEmpty() {
        caseDataBuilder.respondents1(buildRespondents("", "Ross"));

        final String respondentFullName = caseDataBuilder.build().buildFirstRespondentFullName();

        assertThat(respondentFullName).isEqualTo("Ross");
    }

    private ImmutableList<Element<Respondent>> buildRespondents(String firstName, String lastName) {
        return ImmutableList.of(Element.<Respondent>builder()
            .value(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .build())
                .build())
            .build());
    }

}
