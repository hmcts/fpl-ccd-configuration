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

        List<Element<Respondent>> expandedRespondentCollection = service.expandRespondentCollection(caseData);

        assertThat(expandedRespondentCollection).hasSize(1);
        assertThat(expandedRespondentCollection.get(0).getValue().getParty().getPartyId()).isNotNull();
    }

    @Test
    void shouldAddPartyIDAndPartyTypeValuesToSingleRespondent() {
        List<Element<Respondent>> respondents = ImmutableList.of(
            Element.<Respondent>builder()
                .id(UUID.randomUUID())
                .value(Respondent.builder()
                    .party(RespondentParty.builder()
                        .firstName("James")
                        .build())
                    .build())
                .build());

        CaseData caseData = CaseData.builder()
            .respondents1(respondents)
            .build();

        List<Element<Respondent>> updatedRespondents = service.modifyHiddenValues(caseData);

        assertThat(updatedRespondents.get(0).getValue().getParty().getFirstName()).isEqualTo("James");
        assertThat(updatedRespondents.get(0).getValue().getParty().getPartyType()).isEqualTo(INDIVIDUAL);
        assertThat(updatedRespondents.get(0).getValue().getParty().getPartyId()).isNotNull();
    }

    @Test
    void shouldAddPartyIDAndPartyTypeValuesToMultipleRespondents() {
        List<Element<Respondent>> respondents = ImmutableList.of(
            Element.<Respondent>builder()
                .id(UUID.randomUUID())
                .value(Respondent.builder()
                    .party(RespondentParty.builder()
                        .firstName("James")
                        .build())
                    .build())
                .build(),
            Element.<Respondent>builder()
                .id(UUID.randomUUID())
                .value(Respondent.builder()
                    .party(RespondentParty.builder()
                        .firstName("Lucy")
                        .build())
                    .build())
                .build());

        CaseData caseData = CaseData.builder()
            .respondents1(respondents)
            .build();

        List<Element<Respondent>> updatedRespondents = service.modifyHiddenValues(caseData);

        assertThat(updatedRespondents.get(0).getValue().getParty().getFirstName()).isEqualTo("James");
        assertThat(updatedRespondents.get(0).getValue().getParty().getPartyType()).isEqualTo(INDIVIDUAL);
        assertThat(updatedRespondents.get(0).getValue().getParty().getPartyId()).isNotNull();
        assertThat(updatedRespondents.get(1).getValue().getParty().getFirstName()).isEqualTo("Lucy");
        assertThat(updatedRespondents.get(1).getValue().getParty().getPartyType()).isEqualTo(INDIVIDUAL);
        assertThat(updatedRespondents.get(1).getValue().getParty().getPartyId()).isNotNull();
    }

    @Test
    void shouldKeepExistingPartyIDWhenAlreadyExists() {
        List<Element<Respondent>> respondents = ImmutableList.of(
            Element.<Respondent>builder()
                .id(UUID.randomUUID())
                .value(Respondent.builder()
                    .party(RespondentParty.builder()
                        .partyId("123")
                        .build())
                    .build())
                .build());

        CaseData caseData = CaseData.builder()
            .respondents1(respondents)
            .build();

        List<Element<Respondent>> updatedRespondents = service.modifyHiddenValues(caseData);

        assertThat(updatedRespondents.get(0).getValue().getParty().getPartyId()).isEqualTo("123");
    }

    @Test
    void shouldKeepExistingPartyIdAndContinueAddingNewPartyId() {
        List<Element<Respondent>> respondents = ImmutableList.of(
            Element.<Respondent>builder()
                .id(UUID.randomUUID())
                .value(Respondent.builder()
                    .party(RespondentParty.builder()
                        .firstName("James")
                        .partyId("123")
                        .build())
                    .build())
                .build(),
            Element.<Respondent>builder()
                .id(UUID.randomUUID())
                .value(Respondent.builder()
                    .party(RespondentParty.builder()
                        .firstName("Lucy")
                        .build())
                    .build())
                .build());

        CaseData caseData = CaseData.builder()
            .respondents1(respondents)
            .build();

        List<Element<Respondent>> updatedRespondents = service.modifyHiddenValues(caseData);

        assertThat(updatedRespondents.get(0).getValue().getParty().getFirstName()).isEqualTo("James");
        assertThat(updatedRespondents.get(0).getValue().getParty().getPartyId()).isEqualTo("123");
        assertThat(updatedRespondents.get(1).getValue().getParty().getFirstName()).isEqualTo("Lucy");
        assertThat(updatedRespondents.get(1).getValue().getParty().getPartyId()).isNotNull();
    }

    @Test
    void shouldShowContactDetailsOfConfidentialRespondentsWhenExpandingRespondentCollection() {
        UUID id = UUID.randomUUID();
        List<Element<Respondent>> respondents = ImmutableList.of(
            Element.<Respondent>builder()
                .id(id)
                .value(Respondent.builder()
                    .party(RespondentParty.builder()
                        .firstName("James")
                        .contactDetailsHidden("Yes")
                        .build())
                    .build())
                .build());

        List<Element<Respondent>> confidentialRespondents = ImmutableList.of(
            Element.<Respondent>builder()
                .id(id)
                .value(Respondent.builder()
                    .party(RespondentParty.builder()
                        .firstName("James")
                        .contactDetailsHidden("Yes")
                        .address(Address.builder()
                            .addressLine1("James' House")
                            .build())
                        .build())
                    .build())
                .build());

        CaseData caseData = CaseData.builder()
            .respondents1(respondents)
            .confidentialRespondents(confidentialRespondents)
            .build();

        List<Element<Respondent>> expandedRespondentCollection = service.expandRespondentCollection(caseData);

        assertThat(expandedRespondentCollection.get(0).getValue().getParty().getAddress().getAddressLine1()).isEqualTo(
            "James' House");
    }

    @Test
    void shouldHideRespondentContactDetailsWhenConfidentialityFlagSet() {
        List<Element<Respondent>> respondents = ImmutableList.of(
            Element.<Respondent>builder()
                .id(UUID.randomUUID())
                .value(Respondent.builder()
                    .party(RespondentParty.builder()
                        .firstName("James")
                        .contactDetailsHidden("Yes")
                        .address(Address.builder()
                            .addressLine1("James' House")
                            .build())
                        .build())
                    .build())
                .build());

        CaseData caseData = CaseData.builder()
            .respondents1(respondents)
            .build();

        List<Element<Respondent>> updatedRespondents = service.modifyHiddenValues(caseData);

        assertThat(updatedRespondents.get(0).getValue().getParty().getAddress()).isNull();
        assertThat(updatedRespondents.get(0).getValue().getParty().getTelephoneNumber()).isNull();

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
