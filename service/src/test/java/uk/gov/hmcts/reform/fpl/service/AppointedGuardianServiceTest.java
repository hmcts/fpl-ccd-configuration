package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class AppointedGuardianServiceTest {

    private final AppointedGuardianService underTest = new AppointedGuardianService();

    @Test
    void shouldReturnDefaultLabelMessageIfNoRespondentsOrOthers() {
        CaseData caseData = CaseData.builder().appointedGuardianSelector(Selector.builder().build()).build();
        String formattedLabel = underTest.getAppointedGuardiansLabel(caseData);
        assertThat(formattedLabel).isEqualTo("No respondents or others to be given notice on the case");
    }

    @Test
    void shouldReturnFormattedLabelForMultipleRespondentsAndOthers() {
        CaseData caseData = getMultiplePeopleCaseData();
        String expected = "Person 1: Respondent - Remy Respondent\n"
            + "Person 2: Respondent - Tony Stark\n"
            + "Person 3: Other - Ollie Otherworld\n"
            + "Person 4: Other - Otto Otherman\n"
            + "Person 5: Other - Bob Bothers\n";

        String label = underTest.getAppointedGuardiansLabel(caseData);
        assertThat(label).isEqualTo(expected);
    }

    @Test
    void shouldReturnEmptyStringIfNoRespondentsOrOthers() {
        CaseData caseData = CaseData.builder().appointedGuardianSelector(Selector.builder().build()).build();
        String formattedNames = underTest.getAppointedGuardiansNames(caseData);
        assertThat(formattedNames).isEmpty();
    }

    @Test
    void shouldGetAppointedGuardiansNamesForOneRespondent() {
        CaseData caseData = CaseData.builder().respondents1(wrapElements(Respondent.builder()
            .party(RespondentParty.builder().firstName("Remy").lastName("Respondent").build()).build()))
            .appointedGuardianSelector(Selector.builder().selected(List.of(0)).build())
            .build();

        String formattedNames = underTest.getAppointedGuardiansNames(caseData);
        assertThat(formattedNames).isEqualTo("Remy Respondent is");
    }

    @Test
    void shouldGetOnlySelectedNamesFromBothRespondentsAndOthers() {
        CaseData caseData = getMultiplePeopleCaseData();

        String formattedNames = underTest.getAppointedGuardiansNames(caseData);
        assertThat(formattedNames).isEqualTo("Remy Respondent, Otto Otherman, Bob Bothers are");
    }

    private CaseData getMultiplePeopleCaseData() {
        return CaseData.builder().respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().firstName("Remy").lastName("Respondent").build()).build(),
            Respondent.builder()
                .party(RespondentParty.builder().firstName("Tony").lastName("Stark").build()).build()))
            .others(Others.builder()
                .firstOther(Other.builder().name("Ollie Otherworld").build())
                .additionalOthers(wrapElements(Other.builder()
                    .name("Otto Otherman").build(), Other.builder().name("Bob Bothers").build())).build())
            .appointedGuardianSelector(Selector.builder().selected(List.of(0, 3, 4)).build())
            .build();
    }
}
