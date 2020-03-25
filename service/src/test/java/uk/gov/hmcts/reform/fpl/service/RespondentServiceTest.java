package uk.gov.hmcts.reform.fpl.service;

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
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

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
            .respondents1(List.of(respondentWithRemovedConfidentialFields(ID)))
            .build();

        List<Element<Respondent>> respondents = service.prepareRespondents(caseData);

        assertThat(respondents).containsExactly(respondentWithRemovedConfidentialFields(ID));
    }

    @Test
    void shouldPrepareRespondentWithConfidentialValuesWhenConfidentialRespondentIsNotEmpty() {
        CaseData caseData = CaseData.builder()
            .respondents1(List.of(respondentWithRemovedConfidentialFields(ID)))
            .confidentialRespondents(List.of(respondentWithConfidentialFields(ID)))
            .build();

        List<Element<Respondent>> respondents = service.prepareRespondents(caseData);

        assertThat(respondents).containsOnly(respondentWithConfidentialFields(ID));
    }

    @Test
    void shouldReturnRespondentWithoutConfidentialDetailsWhenThereIsNoMatchingConfidentialRespondent() {
        CaseData caseData = CaseData.builder()
            .respondents1(List.of(respondentWithRemovedConfidentialFields(ID)))
            .confidentialRespondents(List.of(respondentWithConfidentialFields(randomUUID())))
            .build();

        List<Element<Respondent>> respondents = service.prepareRespondents(caseData);

        assertThat(respondents).containsOnly(respondentWithRemovedConfidentialFields(ID));
    }

    @Test
    void shouldAddExpectedRespondentWhenHiddenDetailsMarkedAsNo() {
        CaseData caseData = CaseData.builder()
            .respondents1(List.of(respondentWithDetailsHiddenNo(ID)))
            .confidentialRespondents(List.of(respondentWithConfidentialFields(ID)))
            .build();

        List<Element<Respondent>> respondents = service.prepareRespondents(caseData);

        assertThat(respondents).containsOnly(respondentWithDetailsHiddenNo(ID));
    }

    @Test
    void shouldMaintainOrderingOfRespondentWhenComplexScenario() {
        UUID otherId = randomUUID();

        List<Element<Respondent>> respondents = List.of(
            respondentWithRemovedConfidentialFields(ID),
            respondentWithDetailsHiddenNo(randomUUID()),
            respondentWithRemovedConfidentialFields(otherId));

        List<Element<Respondent>> confidentialRespondent = List.of(
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
        List<Element<Respondent>> respondents = List.of(respondentElementWithName("James"));

        CaseData caseData = CaseData.builder()
            .respondents1(respondents)
            .build();

        List<Element<Respondent>> updatedRespondents = service.modifyHiddenValues(caseData.getAllRespondents());

        assertThat(getParty(updatedRespondents, 0)).extracting(FIELDS).containsExactly("James", INDIVIDUAL);
        assertThat(getParty(updatedRespondents, 0).partyId).isNotNull();
    }

    @Test
    void shouldAddPartyIDAndPartyTypeValuesToMultipleRespondents() {
        List<Element<Respondent>> respondents = List.of(
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
        List<Element<Respondent>> respondents = List.of(respondentElementWithId(id));

        CaseData caseData = CaseData.builder()
            .respondents1(respondents)
            .build();

        List<Element<Respondent>> updatedRespondents = service.modifyHiddenValues(caseData.getAllRespondents());

        assertThat(getParty(updatedRespondents, 0).partyId).isEqualTo(id);
    }

    @Test
    void shouldKeepExistingPartyIdAndContinueAddingNewPartyId() {
        String id = "123";
        List<Element<Respondent>> respondents = List.of(
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

    @Test
    void shouldRemoveAllNonConfidentialFieldsWhenPopulatedRespondent() {
        List<Element<Respondent>> respondents = wrapElements(populatedRespondent());

        List<Element<Respondent>> confidentialRespondentDetails = service.retainConfidentialDetails(respondents);

        assertThat(unwrapElements(confidentialRespondentDetails))
            .containsExactly(respondentWithOnlyConfidentialFields());
    }

    private Respondent respondentWithOnlyConfidentialFields() {
        return Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("James")
                .lastName("Smith")
                .email(EmailAddress.builder().email("email@email.com").build())
                .address(Address.builder().addressLine1("Address Line 1").build())
                .telephoneNumber(Telephone.builder().telephoneNumber("01227 831393").build())
                .build())
            .build();
    }

    private Respondent populatedRespondent() {
        return Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("James")
                .lastName("Smith")
                .contactDetailsHidden("Yes")
                .email(EmailAddress.builder().email("email@email.com").build())
                .address(Address.builder().addressLine1("Address Line 1").build())
                .telephoneNumber(Telephone.builder().telephoneNumber("01227 831393").build())
                .gender("Male")
                .litigationIssues("Litigation issues")
                .build())
            .build();
    }

    private List<Element<Respondent>> respondentElementWithDetailsHiddenValue(String hidden) {
        return wrapElements(Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("James")
                .contactDetailsHidden(hidden)
                .email(EmailAddress.builder().email("email@email.com").build())
                .address(Address.builder().addressLine1("Address Line 1").build())
                .telephoneNumber(Telephone.builder().telephoneNumber("01227 831393").build())
                .build())
            .build());
    }

    private Element<Respondent> respondentElementWithName(String name) {
        return element(Respondent.builder()
            .party(RespondentParty.builder()
                .firstName(name)
                .build())
            .build());
    }

    private Element<Respondent> respondentWithDetailsHiddenNo(UUID id) {
        return element(id, Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("James")
                .contactDetailsHidden("No")
                .email(EmailAddress.builder().email("email@email.com").build())
                .address(Address.builder().addressLine1("Address Line 1").build())
                .telephoneNumber(Telephone.builder().telephoneNumber("01227 831393").build())
                .build())
            .build());
    }

    private Element<Respondent> respondentWithRemovedConfidentialFields(UUID id) {
        return element(id, Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("James")
                .contactDetailsHidden("Yes")
                .build())
            .build());
    }

    private Element<Respondent> respondentWithConfidentialFields(UUID id) {
        return element(id, Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("James")
                .contactDetailsHidden("Yes")
                .email(EmailAddress.builder().email("email@email.com").build())
                .address(Address.builder().addressLine1("Address Line 1").build())
                .telephoneNumber(Telephone.builder().telephoneNumber("01227 831393").build())
                .build())
            .build());
    }

    private RespondentParty getParty(List<Element<Respondent>> updatedRespondents, int i) {
        return updatedRespondents.get(i).getValue().getParty();
    }

    private Element<Respondent> respondentElementWithId(String id) {
        return element(Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("James")
                .partyId(id)
                .build())
            .build());
    }

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
