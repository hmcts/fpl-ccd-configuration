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
        CaseData caseData = CaseData.builder().respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().firstName("Remy").lastName("Respondent").build()).build(),
            Respondent.builder()
                .party(RespondentParty.builder().firstName("Tony").lastName("Stark").build()).build()))
            .others(Others.builder()
                .firstOther(Other.builder().name("Ollie Otherworldly").build())
                .additionalOthers(wrapElements(Other.builder()
                    .name("Otto Otherman").build(), Other.builder().name("Bob Bothers").build())).build())
            .appointedGuardianSelector(Selector.builder().selected(List.of(0, 3, 4)).build())
            .build();

        String formattedNames = underTest.getAppointedGuardiansNames(caseData);
        assertThat(formattedNames).isEqualTo("Remy Respondent, Otto Otherman, Bob Bothers are");
    }
}
