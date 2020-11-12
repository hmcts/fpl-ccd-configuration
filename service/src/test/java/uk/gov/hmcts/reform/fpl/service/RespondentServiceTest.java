package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class RespondentServiceTest {

    private final RespondentService service = new RespondentService();

    @Nested
    class BuildRespondentLabel {

        @Test
        void shouldBuildExpectedLabelWhenSingleElementInList() {
            List<Element<Respondent>> respondents = wrapElements(respondent("James", "Daniels"));

            String result = service.buildRespondentLabel(respondents);

            assertThat(result).isEqualTo("Respondent 1 - James Daniels\n");
        }

        @Test
        void shouldBuildExpectedLabelWhenManyElementsInList() {
            List<Element<Respondent>> respondents = respondents();

            String result = service.buildRespondentLabel(respondents);

            assertThat(result).isEqualTo("Respondent 1 - James Daniels\nRespondent 2 - Bob Martyn\n");
        }

        @Test
        void shouldBuildExpectedLabelWhenEmptyList() {
            List<Element<Respondent>> respondents = emptyList();

            String result = service.buildRespondentLabel(respondents);

            assertThat(result).isEqualTo("No respondents on the case");
        }

    }

    @Test
    void shouldPersistRepresentativeAssociation() {
        List<Element<UUID>> association = List.of(element(UUID.randomUUID()));
        Element<Respondent> oldRespondent = element(respondent("dave", "davidson", association));

        List<Element<Respondent>> oldRespondents = List.of(oldRespondent);
        List<Element<Respondent>> newRespondents = List.of(
            element(oldRespondent.getId(), respondent("dave", "davidson")),
            element(respondent("not dave", "not davidson"))
        );

        List<Element<Respondent>> updated = service.persistRepresentativesRelationship(newRespondents, oldRespondents);

        assertThat(updated.get(0).getValue().getRepresentedBy()).isEqualTo(association);
        assertThat(updated.get(1).getValue().getRepresentedBy()).isNullOrEmpty();
    }

    private List<Element<Respondent>> respondents() {
        return wrapElements(respondent("James", "Daniels"), respondent("Bob", "Martyn"));
    }

    private Respondent respondent(String firstName, String lastName) {
        return respondent(firstName, lastName, null);
    }

    private Respondent respondent(String firstName, String lastName, List<Element<UUID>> representedBy) {
        return Respondent.builder()
            .party(RespondentParty.builder()
                .firstName(firstName)
                .lastName(lastName)
                .build())
            .representedBy(representedBy)
            .build();
    }

}
