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
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;

import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.PartyType.INDIVIDUAL;

@ExtendWith(SpringExtension.class)
class RespondentServiceTest {
    private static final UUID ID = randomUUID();
    private static final String[] FIELDS = {"firstName", "partyType"};

    private final RespondentService service = new RespondentService();

    @Test
    void shouldAddEmptyElementWhenRespondentIsEmpty() {
        List<Element<Respondent>> respondents = service.prepareRespondents(CaseData.builder().build());

        assertThat(getParty(respondents, 0).partyId).isNotNull();
    }

    @Test
    void shouldReturnRespondentsIfRespondentsIsPrePopulated() {
        CaseData caseData = CaseData.builder()
            .respondents1(ImmutableList.of(respondentWithRemovedConfidentialFields(ID)))
            .build();

        List<Element<Respondent>> respondents = service.prepareRespondents(caseData);

        assertThat(respondents).containsExactly(respondentWithRemovedConfidentialFields(ID));
    }

    @Test
    void shouldPrepareRespondentWithConfidentialValuesWhenConfidentialRespondentIsNotEmpty() {
        CaseData caseData = CaseData.builder()
            .respondents1(ImmutableList.of(respondentWithRemovedConfidentialFields(ID)))
            .confidentialRespondents(ImmutableList.of(respondentWithConfidentialFields(ID)))
            .build();

        List<Element<Respondent>> respondents = service.prepareRespondents(caseData);

        assertThat(respondents).containsOnly(respondentWithConfidentialFields(ID));
    }

    @Test
    void shouldReturnRespondentWithoutConfidentialDetailsWhenThereIsNoMatchingConfidentialRespondent() {
        CaseData caseData = CaseData.builder()
            .respondents1(ImmutableList.of(respondentWithRemovedConfidentialFields(ID)))
            .confidentialRespondents(ImmutableList.of(respondentWithConfidentialFields(randomUUID())))
            .build();

        List<Element<Respondent>> respondents = service.prepareRespondents(caseData);

        assertThat(respondents).containsOnly(respondentWithRemovedConfidentialFields(ID));
    }

    @Test
    void shouldAddExpectedRespondentWhenHiddenDetailsMarkedAsNo() {
        CaseData caseData = CaseData.builder()
            .respondents1(ImmutableList.of(respondentWithDetailsHiddenNo(ID)))
            .confidentialRespondents(ImmutableList.of(respondentWithConfidentialFields(ID)))
            .build();

        List<Element<Respondent>> respondents = service.prepareRespondents(caseData);

        assertThat(respondents).containsOnly(respondentWithDetailsHiddenNo(ID));
    }

    @Test
    void shouldMaintainOrderingOfRespondentWhenComplexScenario() {
        UUID otherId = randomUUID();

        List<Element<Respondent>> respondents = ImmutableList.of(
            respondentWithRemovedConfidentialFields(ID),
            respondentWithDetailsHiddenNo(randomUUID()),
            respondentWithRemovedConfidentialFields(otherId));

        List<Element<Respondent>> confidentialRespondent = ImmutableList.of(
            respondentWithConfidentialFields(ID),
            respondentWithConfidentialFields(otherId));

        CaseData caseData = CaseData.builder()
            .respondents1(respondents)
            .confidentialRespondents(confidentialRespondent)
            .build();

        List<Element<Respondent>> updatedRespondent = service.prepareRespondents(caseData);

        assertThat(updatedRespondent.get(0)).isEqualTo(confidentialRespondent.get(0));
        assertThat(updatedRespondent.get(1)).isEqualTo(respondents.get(1));
        assertThat(updatedRespondent.get(2)).isEqualTo(confidentialRespondent.get(1));
    }

    @Test
    void shouldAddPartyIDAndPartyTypeValuesToSingleRespondent() {
        List<Element<Respondent>> respondents = ImmutableList.of(respondentElementWithName("James"));

        CaseData caseData = CaseData.builder()
            .respondents1(respondents)
            .build();

        List<Element<Respondent>> updatedRespondents = service.modifyHiddenValues(caseData.getAllRespondents());

        assertThat(getParty(updatedRespondents, 0)).extracting(FIELDS).containsExactly("James", INDIVIDUAL);
        assertThat(getParty(updatedRespondents, 0).partyId).isNotNull();
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

        assertThat(getParty(updatedRespondents, 0)).extracting(FIELDS).containsExactly("James", INDIVIDUAL);
        assertThat(getParty(updatedRespondents, 0).partyId).isNotNull();

        assertThat(getParty(updatedRespondents, 1)).extracting(FIELDS).containsExactly("Lucy", INDIVIDUAL);
        assertThat(getParty(updatedRespondents, 1).partyId).isNotNull();
    }

    @Test
    void shouldKeepExistingPartyIDWhenAlreadyExists() {
        String id = "123";
        List<Element<Respondent>> respondents = ImmutableList.of(respondentElementWithId(id));

        CaseData caseData = CaseData.builder()
            .respondents1(respondents)
            .build();

        List<Element<Respondent>> updatedRespondents = service.modifyHiddenValues(caseData.getAllRespondents());

        assertThat(getParty(updatedRespondents, 0).partyId).isEqualTo(id);
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

        assertThat(getParty(updatedRespondents, 0).firstName).isEqualTo("James");
        assertThat(getParty(updatedRespondents, 0).partyId).isEqualTo(id);
        assertThat(getParty(updatedRespondents, 1).firstName).isEqualTo("Lucy");
        assertThat(getParty(updatedRespondents, 1).partyId).isNotNull();
    }

    @Test
    void shouldHideRespondentContactDetailsWhenConfidentialityFlagSet() {
        List<Element<Respondent>> respondents = respondentElementWithDetailsHiddenValue("Yes");

        CaseData caseData = CaseData.builder()
            .respondents1(respondents)
            .build();

        List<Element<Respondent>> updatedRespondents = service.modifyHiddenValues(caseData.getAllRespondents());

        assertThat(getParty(updatedRespondents, 0).address).isNull();
        assertThat(getParty(updatedRespondents, 0).email).isNull();
        assertThat(getParty(updatedRespondents, 0).telephoneNumber).isNull();
    }

    @Test
    void shouldNotHideRespondentContactDetailsWhenConfidentialityFlagSet() {
        List<Element<Respondent>> respondents = respondentElementWithDetailsHiddenValue("No");

        CaseData caseData = CaseData.builder()
            .respondents1(respondents)
            .build();

        List<Element<Respondent>> updatedRespondents = service.modifyHiddenValues(caseData.getAllRespondents());

        assertThat(getParty(updatedRespondents, 0).address).isNotNull();
        assertThat(getParty(updatedRespondents, 0).email).isNotNull();
        assertThat(getParty(updatedRespondents, 0).telephoneNumber).isNotNull();
    }

    private List<Element<Respondent>> respondentElementWithDetailsHiddenValue(String hidden) {
        return ImmutableList.of(Element.<Respondent>builder()
            .id(randomUUID())
            .value(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("James")
                    .contactDetailsHidden(hidden)
                    .email(EmailAddress.builder().email("email@email.com").build())
                    .address(Address.builder()
                        .addressLine1("Address Line 1")
                        .build())
                    .telephoneNumber(Telephone.builder().telephoneNumber("01227 831393").build())
                    .build())
                .build())
            .build());
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

    private Element<Respondent> respondentWithDetailsHiddenNo(UUID id) {
        return Element.<Respondent>builder()
            .id(id)
            .value(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("James")
                    .contactDetailsHidden("No")
                    .email(EmailAddress.builder().email("email@email.com").build())
                    .address(Address.builder()
                        .addressLine1("Address Line 1")
                        .build())
                    .telephoneNumber(Telephone.builder().telephoneNumber("01227 831393").build())
                    .build())
                .build())
            .build();
    }

    private Element<Respondent> respondentWithRemovedConfidentialFields(UUID id) {
        return Element.<Respondent>builder()
            .id(id)
            .value(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("James")
                    .contactDetailsHidden("Yes")
                    .build())
                .build())
            .build();
    }

    private Element<Respondent> respondentWithConfidentialFields(UUID id) {
        return Element.<Respondent>builder()
            .id(id)
            .value(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("James")
                    .contactDetailsHidden("Yes")
                    .email(EmailAddress.builder().email("email@email.com").build())
                    .address(Address.builder()
                        .addressLine1("Address Line 1")
                        .build())
                    .telephoneNumber(Telephone.builder().telephoneNumber("01227 831393").build())
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
