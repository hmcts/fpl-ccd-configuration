package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.RespondentsRefusedFormatter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class RespondentsRefusedFormatterTest {

    private final RespondentsRefusedFormatter underTest = new RespondentsRefusedFormatter();

    @Nested
    class RespondentsLabel {

        @Test
        void shouldReturnDefaultLabelMessageIfNoRespondentsOrOthers() {
            CaseData caseData = CaseData.builder().respondentsRefusedSelector(Selector.builder().build()).build();
            String formattedLabel = underTest.getRespondentsRefusedLabel(caseData);
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

            String label = underTest.getRespondentsRefusedLabel(caseData);
            assertThat(label).isEqualTo(expected);
        }
    }

    @Nested
    class RespondentsNamesForDocument {

        @Test
        void shouldReturnEmptyStringIfNoRespondentsOrOthers() {
            CaseData caseData = CaseData.builder().respondentsRefusedSelector(Selector.builder().build()).build();
            String formattedNames = underTest.getRespondentsNamesForDocument(caseData);
            assertThat(formattedNames).isEmpty();
        }

        @Test
        void shouldGetRespondentsRefusedNamesForDocumentWhenOneRespondent() {
            CaseData caseData = CaseData.builder().respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().firstName("Remy").lastName("Respondy").build()).build()))
                .respondentsRefusedSelector(Selector.builder().selected(List.of(0)).build())
                .build();

            String formattedNames = underTest.getRespondentsNamesForDocument(caseData);
            assertThat(formattedNames).isEqualTo("Remy Respondy");
        }

        @Test
        void shouldGetOnlySelectedNamesForDocumentWhenBothRespondentsAndOthersPresent() {
            CaseData caseData = getMultiplePeopleCaseData();

            String formattedNames = underTest.getRespondentsNamesForDocument(caseData);
            assertThat(formattedNames).isEqualTo("Remy Respondy, Otto Otherman and Bob Bothers");
        }
    }

    @Nested
    class RespondentsNamesForTab {

        @Test
        void shouldReturnNullForTabWhenNoOneSelected() {
            CaseData caseData = CaseData.builder().build();

            String formattedNames = underTest.getRespondentsNamesForTab(caseData);
            assertThat(formattedNames).isNull();
        }

        @Test
        void shouldGetRespondentsRefusedNamesForTabWhenOneRespondent() {
            CaseData caseData = CaseData.builder().respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().firstName("Remy").lastName("Respondy").build()).build()))
                .respondentsRefusedSelector(Selector.builder().selected(List.of(0)).build())
                .build();

            String formattedNames = underTest.getRespondentsNamesForTab(caseData);
            assertThat(formattedNames).isEqualTo("Remy Respondy");
        }

        @Test
        void shouldGetOnlySelectedNamesForTabWhenBothRespondentsAndOthersPresent() {
            CaseData caseData = getMultiplePeopleCaseData();

            String formattedNames = underTest.getRespondentsNamesForTab(caseData);
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
                Other.builder().name("Otto Otherman").build(),
                Other.builder().firstName("Bob").lastName("Bothers").build()))
            .respondentsRefusedSelector(Selector.builder().selected(List.of(0, 3, 4)).build())
            .build();
    }
}
