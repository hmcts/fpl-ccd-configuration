package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;

import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.PartyType.INDIVIDUAL;

@ExtendWith(SpringExtension.class)
class RespondentServiceTest {
    private final RespondentService service = new RespondentService();

    @Test
    void shouldExpandRespondentCollectionWhenNoRespondents() {
        CaseData caseData = CaseData.builder().build();

        List<Element<Respondent>> expandedRespondentCollection = service.expandCollection(caseData.getAllRespondents());

        assertThat(expandedRespondentCollection).hasSize(1);
        assertThat(getParty(expandedRespondentCollection, 0).getPartyId()).isNotNull();
    }

    @Test
    void shouldAddPartyIDAndPartyTypeValuesToSingleRespondent() {
        List<Element<Respondent>> respondents = ImmutableList.of(respondentElementWithName("James"));

        CaseData caseData = CaseData.builder()
            .respondents1(respondents)
            .build();

        List<Element<Respondent>> updatedRespondents = service.modifyHiddenValues(caseData.getAllRespondents());

        assertThat(getParty(updatedRespondents, 0)).extracting("firstName", "partyType").containsExactly("James", INDIVIDUAL);
        assertThat(getParty(updatedRespondents, 0).getPartyId()).isNotNull();
    }

    @Test
    void shouldAddPartyIDAndPartyTypeValuesToMultipleRespondents() {
        List<Element<Respondent>> respondents = ImmutableList.of(
            respondentElementWithName("James"),
            respondentElementWithName("Lucy"));

        CaseData caseData = CaseData.builder()
            .respondents1(respondents)
            .build();

        List<Element<Respondent>> updatedRespondents = service.modifyHiddenValues(caseData.getAllRespondents());

        assertThat(getParty(updatedRespondents, 0)).extracting("firstName", "partyType").containsExactly("James", INDIVIDUAL);
        assertThat(getParty(updatedRespondents, 0).getPartyId()).isNotNull();

        assertThat(getParty(updatedRespondents, 1)).extracting("firstName", "partyType").containsExactly("Lucy", INDIVIDUAL);
        assertThat(getParty(updatedRespondents, 1).getPartyId()).isNotNull();
    }

    @Test
    void shouldKeepExistingPartyIDWhenAlreadyExists() {
        String id = "123";
        List<Element<Respondent>> respondents = ImmutableList.of(respondentElementWithId(id));

        CaseData caseData = CaseData.builder()
            .respondents1(respondents)
            .build();

        List<Element<Respondent>> updatedRespondents = service.modifyHiddenValues(caseData.getAllRespondents());

        assertThat(getParty(updatedRespondents, 0).getPartyId()).isEqualTo(id);
    }

    @Test
    void shouldKeepExistingPartyIdAndContinueAddingNewPartyId() {
        String id = "123";
        List<Element<Respondent>> respondents = ImmutableList.of(
            respondentElementWithId(id),
            respondentElementWithName("Lucy"));

        CaseData caseData = CaseData.builder()
            .respondents1(respondents)
            .build();

        List<Element<Respondent>> updatedRespondents = service.modifyHiddenValues(caseData.getAllRespondents());

        assertThat(getParty(updatedRespondents, 0).getFirstName()).isEqualTo("James");
        assertThat(getParty(updatedRespondents, 0).getPartyId()).isEqualTo(id);
        assertThat(getParty(updatedRespondents, 1).getFirstName()).isEqualTo("Lucy");
        assertThat(getParty(updatedRespondents, 1).getPartyId()).isNotNull();
    }

    @Test
    void shouldHideRespondentContactDetailsWhenConfidentialityFlagSet() {
        List<Element<Respondent>> respondents = ImmutableList.of(
            respondentElementWithDetailsHiddenValue("Yes"));

        CaseData caseData = CaseData.builder()
            .respondents1(respondents)
            .build();

        List<Element<Respondent>> updatedRespondents = service.modifyHiddenValues(caseData.getAllRespondents());

        assertThat(getParty(updatedRespondents, 0).getAddress()).isNull();
        assertThat(getParty(updatedRespondents, 0).getTelephoneNumber()).isNull();
    }

    @Test
    void shouldNotHideRespondentContactDetailsWhenConfidentialityFlagSet() {
        List<Element<Respondent>> respondents = ImmutableList.of(
            respondentElementWithDetailsHiddenValue("No"));

        CaseData caseData = CaseData.builder()
            .respondents1(respondents)
            .build();

        List<Element<Respondent>> updatedRespondents = service.modifyHiddenValues(caseData.getAllRespondents());

        assertThat(getParty(updatedRespondents, 0).getAddress()).isNotNull();
        assertThat(getParty(updatedRespondents, 0).getTelephoneNumber()).isNotNull();
    }

    private Element<Respondent> respondentElementWithDetailsHiddenValue(String hidden) {
        return Element.<Respondent>builder()
            .id(UUID.randomUUID())
            .value(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("James")
                    .contactDetailsHidden(hidden)
                    .address(Address.builder()
                        .addressLine1("James' House")
                        .build())
                    .telephoneNumber(Telephone.builder().telephoneNumber("01227 831393").build())
                    .build())
                .build())
            .build();
    }

    private Element<Respondent> respondentElementWithName(String name) {
        return Element.<Respondent>builder()
            .id(UUID.randomUUID())
            .value(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName(name)
                    .build())
                .build())
            .build();
    }

    private RespondentParty getParty(List<Element<Respondent>> updatedRespondents, int i) {
        return updatedRespondents.get(i).getValue().getParty();
    }

    private Element<Respondent> respondentElementWithId(String id) {
        return Element.<Respondent>builder()
            .id(UUID.randomUUID())
            .value(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("James")
                    .partyId(id)
                    .build())
                .build())
            .build();
    }

    @Nested
    class BuildRespondentLabel {

        @Test
        void shouldBuildExpectedLabelWhenSingleElementInList() {
            List<Element<Respondent>> respondents = ImmutableList.of(Element.<Respondent>builder()
                .value(Respondent.builder()
                    .party(RespondentParty.builder()
                        .firstName("James")
                        .lastName("Daniels")
                        .build())
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

        private List<Element<Respondent>> getRespondents() {
            return ImmutableList.of(Element.<Respondent>builder()
                    .value(Respondent.builder()
                        .party(RespondentParty.builder()
                            .firstName("James")
                            .lastName("Daniels")
                            .build())
                        .build())
                    .build(),
                Element.<Respondent>builder()
                    .value(Respondent.builder()
                        .party(RespondentParty.builder()
                            .firstName("Bob")
                            .lastName("Martyn")
                            .build())
                        .build())
                    .build());
        }
    }
}
