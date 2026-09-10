package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class AppointedGuardianFormatterTest {

    private final AppointedGuardianFormatter underTest = new AppointedGuardianFormatter();

    @Nested
    class GuardiansLabel {

        @Test
        void shouldReturnDefaultLabelMessageIfNoRespondentsOrOthers() {
            CaseData caseData = CaseData.builder().appointedGuardianSelector(Selector.builder().build()).build();
            String formattedLabel = underTest.getGuardiansLabel(caseData);
            assertThat(formattedLabel).isEqualTo("No respondents or others to be given notice on the case");
        }

        @Test
        void shouldReturnFormattedLabelForMultipleRespondentsAndOthers() {
            CaseData caseData = getMultiplePeopleCaseData();
            String expected = "Person 1: Respondent - Remy Respondy\n"
                + "Person 2: Respondent - Tony Stark\n"
                + "Person 3: Other - Ollie Otherworld\n"
                + "Person 4: Other - Otto Otherman\n"
                + "Person 5: Other - Bob Bothers\n";

            String label = underTest.getGuardiansLabel(caseData);
            assertThat(label).isEqualTo(expected);
        }
    }

    @Nested
    class GuardiansNamesForDocument {

        @Test
        void shouldReturnEmptyStringIfNoRespondentsOrOthers() {
            CaseData caseData = CaseData.builder().appointedGuardianSelector(Selector.builder().build()).build();
            String formattedNames = underTest.getGuardiansNamesForDocument(caseData);
            assertThat(formattedNames).isEmpty();
        }

        @Test
        void shouldGetAppointedGuardiansNamesForDocumentWhenOneRespondent() {
            CaseData caseData = CaseData.builder().respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().firstName("Remy").lastName("Respondy").build()).build()))
                .appointedGuardianSelector(Selector.builder().selected(List.of(0)).build())
                .build();

            String formattedNames = underTest.getGuardiansNamesForDocument(caseData);
            assertThat(formattedNames).isEqualTo("Remy Respondy is appointed as special guardian");
        }

        @Test
        void shouldGetBothSelectedNamesForDocumentWhenBothRespondents() {
            CaseData caseData = CaseData.builder().respondents1(wrapElements(Respondent.builder()
                    .party(RespondentParty.builder().firstName("Remy").lastName("Respondy").build()).build(),
                    Respondent.builder()
                        .party(RespondentParty.builder().firstName("Otto").lastName("Otherman").build()).build()))
                .appointedGuardianSelector(Selector.builder().selected(List.of(0, 1)).build())
                .build();

            String formattedNames = underTest.getGuardiansNamesForDocument(caseData);
            assertThat(formattedNames)
                .isEqualTo("Remy Respondy and Otto Otherman are appointed as special guardians");
        }

        @Test
        void shouldGetOnlySelectedNamesForDocumentWhenBothRespondentsAndOthersPresent() {
            CaseData caseData = getMultiplePeopleCaseData();

            String formattedNames = underTest.getGuardiansNamesForDocument(caseData);
            assertThat(formattedNames)
                .isEqualTo("Remy Respondy, Otto Otherman and Bob Bothers are appointed as special guardians");
        }

        @Test
        void shouldGetBothSelectedNamesForDocumentWhenBothRespondentsAndAdditionalAppointedGuardians() {
            CaseData caseData = CaseData.builder().respondents1(wrapElements(Respondent.builder()
                        .party(RespondentParty.builder().firstName("Remy").lastName("Respondy").build()).build(),
                    Respondent.builder()
                        .party(RespondentParty.builder().firstName("Otto").lastName("Otherman").build()).build()))
                .appointedGuardianSelector(Selector.builder().selected(List.of(0, 1)).build())
                .manageOrdersEventData(ManageOrdersEventData.builder()
                    .additionalAppointedSpecialGuardians("Mummy Pig").build())
                .build();

            String formattedNames = underTest.getGuardiansNamesForDocument(caseData);
            assertThat(formattedNames).isEqualTo("Remy Respondy, Otto Otherman and Mummy Pig "
                + "are appointed as special guardians");
        }

        @Test
        void shouldGetBothSelectedNamesForDocumentWhenBothRespondentsAndTwoAdditionalAppointedGuardians() {
            CaseData caseData = CaseData.builder().respondents1(wrapElements(Respondent.builder()
                        .party(RespondentParty.builder().firstName("Remy").lastName("Respondy").build()).build(),
                    Respondent.builder()
                        .party(RespondentParty.builder().firstName("Otto").lastName("Otherman").build()).build()))
                .appointedGuardianSelector(Selector.builder().selected(List.of(0, 1)).build())
                .manageOrdersEventData(ManageOrdersEventData.builder()
                    .additionalAppointedSpecialGuardians("Mummy Pig\nPeppa Pig").build())
                .build();

            String formattedNames = underTest.getGuardiansNamesForDocument(caseData);
            assertThat(formattedNames).isEqualTo("Remy Respondy, Otto Otherman, Mummy Pig and Peppa Pig "
                + "are appointed as special guardians");
        }

        @Test
        void shouldGetSingleAdditionalAppointedGuardians() {
            CaseData caseData = CaseData.builder().respondents1(wrapElements(Respondent.builder()
                        .party(RespondentParty.builder().firstName("Remy").lastName("Respondy").build()).build(),
                    Respondent.builder()
                        .party(RespondentParty.builder().firstName("Otto").lastName("Otherman").build()).build()))
                .appointedGuardianSelector(Selector.builder().selected(List.of()).build())
                .manageOrdersEventData(ManageOrdersEventData.builder()
                    .additionalAppointedSpecialGuardians("Mummy Pig").build())
                .build();

            String formattedNames = underTest.getGuardiansNamesForDocument(caseData);
            assertThat(formattedNames).isEqualTo("Mummy Pig is appointed as special guardian");
        }

        @Test
        void shouldGetTwoAdditionalAppointedGuardians() {
            CaseData caseData = CaseData.builder().respondents1(wrapElements(Respondent.builder()
                        .party(RespondentParty.builder().firstName("Remy").lastName("Respondy").build()).build(),
                    Respondent.builder()
                        .party(RespondentParty.builder().firstName("Otto").lastName("Otherman").build()).build()))
                .appointedGuardianSelector(Selector.builder().selected(List.of()).build())
                .manageOrdersEventData(ManageOrdersEventData.builder()
                    .additionalAppointedSpecialGuardians("Mummy Pig\nPeppa Pig").build())
                .build();

            String formattedNames = underTest.getGuardiansNamesForDocument(caseData);
            assertThat(formattedNames)
                .isEqualTo("Mummy Pig and Peppa Pig are appointed as special guardians");
        }
    }

    @Nested
    class GuardiansNamesForTab {

        @Test
        void shouldReturnNullForTabWhenNoOneSelected() {
            CaseData caseData = CaseData.builder().build();

            String formattedNames = underTest.getGuardiansNamesForTab(caseData);
            assertThat(formattedNames).isNull();
        }

        @Test
        void shouldGetAppointedGuardiansNamesForTabWhenOneRespondent() {
            CaseData caseData = CaseData.builder().respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().firstName("Remy").lastName("Respondy").build()).build()))
                .appointedGuardianSelector(Selector.builder().selected(List.of(0)).build())
                .build();

            String formattedNames = underTest.getGuardiansNamesForTab(caseData);
            assertThat(formattedNames).isEqualTo("Remy Respondy");
        }

        @Test
        void shouldGetOnlySelectedNamesForTabWhenBothRespondentsAndOthersPresent() {
            CaseData caseData = getMultiplePeopleCaseData();

            String formattedNames = underTest.getGuardiansNamesForTab(caseData);
            assertThat(formattedNames).isEqualTo("Remy Respondy, Otto Otherman and Bob Bothers");
        }
    }

    private CaseData getMultiplePeopleCaseData() {
        return CaseData.builder().respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().firstName("Remy").lastName("Respondy").build()).build(),
            Respondent.builder()
                .party(RespondentParty.builder().firstName("Tony").lastName("Stark").build()).build()))
            .othersV2(wrapElements(
                Other.builder().firstName("Ollie Otherworld").build(),
                Other.builder().firstName("Otto Otherman").build(),
                Other.builder().firstName("Bob Bothers").build()))
            .appointedGuardianSelector(Selector.builder().selected(List.of(0, 3, 4)).build())
            .build();
    }
}
