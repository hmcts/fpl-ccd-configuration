package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
class RespondentServiceTest {

    private RespondentService service = new RespondentService();

    @Nested
    class BuildRespondentLabel {

        @Test
        void shouldBuildExpectedLabelWhenSingleElementInList() {
            List<Element<Respondent>> respondents = wrapElements(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("James")
                    .lastName("Daniels")
                    .build())
                .build());

            String result = service.buildRespondentLabel(respondents);

            assertThat(result).isEqualTo("Respondent 1 - James Daniels\n");
        }

        @Test
        void shouldBuildExpectedLabelWhenManyElementsInList() {
            List<Element<Respondent>> respondents = getRespondents();

            String result = service.buildRespondentLabel(respondents);

            assertThat(result).isEqualTo("Respondent 1 - James Daniels\nRespondent 2 - Bob Martyn\n");
        }

        @Test
        void shouldBuildExpectedLabelWhenEmptyList() {
            List<Element<Respondent>> respondents = emptyList();

            String result = service.buildRespondentLabel(respondents);

            assertThat(result).isEqualTo("No respondents on the case");
        }

        @Test
        void shouldSetPersistRepresentativeFlagToYes() {
            List<Element<Respondent>> updatedRespondents = service.setPersistRepresentativeFlag(getRespondents());
            List<Respondent> respondentsList = unwrapElements(updatedRespondents);

            assertThat(respondentsList.get(0).getPersistRepresentedBy()).isEqualTo(YES.getValue());
            assertThat(respondentsList.get(1).getPersistRepresentedBy()).isEqualTo(YES.getValue());
        }

        private List<Element<Respondent>> getRespondents() {
            return wrapElements(Respondent.builder()
                    .party(RespondentParty.builder()
                        .firstName("James")
                        .lastName("Daniels")
                        .build())
                    .build(),
                Respondent.builder()
                    .party(RespondentParty.builder()
                        .firstName("Bob")
                        .lastName("Martyn")
                        .build())
                    .build());
        }
    }
}
