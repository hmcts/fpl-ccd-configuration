package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.formatRepresentativesForPostNotification;
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
    void shouldFormatAllRepresentatives() {
        Representative representative1 = Representative.builder()
            .fullName("John Smith")
            .address(Address.builder()
                .addressLine1("A1")
                .postcode("CR0 2GE")
                .build())
            .build();
        Representative representative2 = Representative.builder()
            .fullName("Adam Black")
            .address(Address.builder()
                .addressLine1("Flat 2")
                .postcode("SE16 AB1")
                .build())
            .build();

        List<Representative> representatives = List.of(representative1, representative2);

        List<String> formattedRepresentatives = formatRepresentativesForPostNotification(representatives);

        assertThat(formattedRepresentatives)
            .containsExactlyInAnyOrder("John Smith\nA1, CR0 2GE", "Adam Black\nFlat 2, SE16 AB1");
    }

    @Test
    void shouldReturnEmptyStringWhenNoRepresentatives() {

        List<String> formattedRepresentatives = formatRepresentativesForPostNotification(Collections.emptyList());

        assertThat(formattedRepresentatives).isEmpty();
    }
}
