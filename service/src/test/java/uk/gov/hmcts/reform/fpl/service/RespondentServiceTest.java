package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.UnregisteredOrganisation;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTime;

import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.ccd.model.ChangeOrganisationApprovalStatus.APPROVED;
import static uk.gov.hmcts.reform.ccd.model.Organisation.organisation;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.caseRoleDynamicList;

@ExtendWith(MockitoExtension.class)
class RespondentServiceTest {

    @Spy
    private Time time = new FixedTime();

    @InjectMocks
    private RespondentService service;

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

    @Test
    void shouldGetRespondentsWithLegalRepresentation() {
        CaseData caseData = CaseData.builder()
            .respondents1(wrapElements(buildRespondent(YES.getValue(), "email-1@test.com"),
                buildRespondent(NO.getValue(), "email-2@test.com")))
            .build();

        List<Respondent> respondentsWithLegalRepresentation = service.getRespondentsWithLegalRepresentation(caseData
            .getRespondents1());

        assertThat(respondentsWithLegalRepresentation).size().isEqualTo(1);
    }

    @Test
    void shouldGetRespondentSolicitorEmails() {
        List<Respondent> respondents = List.of(buildRespondent(YES.getValue(), "email-1@test.com"),
            buildRespondent(YES.getValue(), "email-2@test.com"));

        List<String> respondentSolicitorEmails = service.getRespondentSolicitorEmails(respondents);

        assertThat(respondentSolicitorEmails).containsExactlyInAnyOrder("email-1@test.com", "email-2@test.com");
    }

    @Test
    void shouldRemoveSolicitorDetailsWhenRespondentDoesNotNeedRepresentation() {
        List<Element<Respondent>> respondents = List.of(element(Respondent.builder()
            .legalRepresentation(NO.getValue())
            .solicitor(RespondentSolicitor.builder().firstName("Steven").build())
            .build()));

        List<Element<Respondent>> updatedRespondents = service.removeHiddenFields(respondents);
        assertThat(updatedRespondents.get(0).getValue().getSolicitor()).isNull();
    }

    @Test
    void shouldRemoveUnregisteredOrganisationDetailsWhenOrganisationSelected() {
        List<Element<Respondent>> respondents = List.of(element(Respondent.builder()
            .legalRepresentation(YES.getValue())
            .solicitor(RespondentSolicitor.builder()
                .firstName("Steven")
                .organisation(Organisation.builder().organisationID("test id").build())
                .unregisteredOrganisation(UnregisteredOrganisation.builder().name("this should be removed").build())
                .build())
            .build()));

        List<Element<Respondent>> updatedRespondents = service.removeHiddenFields(respondents);
        assertThat(updatedRespondents.get(0).getValue().getSolicitor().getUnregisteredOrganisation()).isNull();
    }

    @Test
    void shouldNotRemoveUnregisteredOrganisationDetailsWhenNoOrganisationIdSelected() {
        UnregisteredOrganisation unregisteredOrg = UnregisteredOrganisation.builder()
            .name("this should not be removed")
            .build();

        List<Element<Respondent>> respondents = List.of(element(Respondent.builder()
            .legalRepresentation(YES.getValue())
            .solicitor(RespondentSolicitor.builder()
                .firstName("Steven")
                .organisation(Organisation.builder().build())
                .unregisteredOrganisation(unregisteredOrg)
                .build())
            .build()));

        List<Element<Respondent>> updatedRespondents = service.removeHiddenFields(respondents);
        assertThat(updatedRespondents.get(0).getValue().getSolicitor().getUnregisteredOrganisation())
            .isEqualTo(unregisteredOrg);
    }

    @Test
    void shouldRemoveRegionalOfficeWhenOrganisationNotSelected() {
        List<Element<Respondent>> respondents = List.of(element(Respondent.builder()
            .legalRepresentation(YES.getValue())
            .solicitor(RespondentSolicitor.builder()
                .firstName("Steven")
                .regionalOfficeAddress(Address.builder().addressLine1("this should be removed").build())
                .build())
            .build()));

        List<Element<Respondent>> updatedRespondents = service.removeHiddenFields(respondents);
        assertThat(updatedRespondents.get(0).getValue().getSolicitor().getRegionalOfficeAddress()).isNull();
    }

    @Test
    void shouldReturnRegisteredSolicitor() {
        RespondentSolicitor registeredSolicitor = RespondentSolicitor.builder()
            .firstName("Steven")
            .organisation(Organisation.builder().organisationID("Organisation ID").build())
            .build();

        RespondentSolicitor unregisteredSolicitor = RespondentSolicitor.builder()
            .firstName("Andrew")
            .unregisteredOrganisation(UnregisteredOrganisation.builder().name("unregistered org").build())
            .build();

        List<Element<Respondent>> respondents = List.of(element(Respondent.builder()
                .legalRepresentation(YES.getValue())
                .solicitor(registeredSolicitor)
                .build()),
            element(Respondent.builder()
                .legalRepresentation(YES.getValue())
                .solicitor(unregisteredSolicitor)
                .build()));

        List<RespondentSolicitor> registeredSolicitors = service.getRegisteredSolicitors(respondents);

        assertThat(registeredSolicitors).containsOnly(registeredSolicitor);
    }

    @Test
    void shouldReturnUnregisteredSolicitor() {
        RespondentSolicitor unregisteredSolicitor = RespondentSolicitor.builder()
            .firstName("Steven")
            .unregisteredOrganisation(UnregisteredOrganisation.builder().name("unregistered org").build())
            .build();

        RespondentSolicitor registeredSolicitor = RespondentSolicitor.builder()
            .firstName("Andrew")
            .organisation(Organisation.builder().organisationID("Organisation ID").build())
            .build();

        List<Element<Respondent>> respondents = List.of(element(Respondent.builder()
                .legalRepresentation(YES.getValue())
                .solicitor(unregisteredSolicitor)
                .build()),
            element(Respondent.builder()
                .legalRepresentation(YES.getValue())
                .solicitor(registeredSolicitor)
                .build()));

        List<RespondentSolicitor> unregisteredSolicitors = service.getUnregisteredSolicitors(respondents);

        assertThat(unregisteredSolicitors).containsOnly(unregisteredSolicitor);
    }

    @Test
    void shouldReturnEmptyWhenNoLegalRepresentation() {
        List<Element<Respondent>> respondents = List.of(element(Respondent.builder()
                .legalRepresentation(NO.getValue())
                .build()),
            element(Respondent.builder()
                .legalRepresentation(NO.getValue())
                .build()));

        List<RespondentSolicitor> registeredSolicitors = service.getRegisteredSolicitors(respondents);
        List<RespondentSolicitor> unregisteredSolicitors = service.getUnregisteredSolicitors(respondents);

        assertThat(registeredSolicitors).isEmpty();
        assertThat(unregisteredSolicitors).isEmpty();
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

    private Respondent buildRespondent(String legalRepresentation, String email) {
        return Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Test respondent")
                .build())
            .legalRepresentation(legalRepresentation)
            .solicitor(buildRespondentSolicitor(email))
            .build();
    }

    private RespondentSolicitor buildRespondentSolicitor(String email) {
        return RespondentSolicitor.builder()
            .firstName("Test respondent solicitor")
            .email(email)
            .build();
    }

    @Nested
    class RepresentationChanges {

        @Test
        void shouldReturnEmptyListOfChangesWhenRespondentsNotPresent() {
            List<ChangeOrganisationRequest> changes = service.getRepresentationChanges(null, null);

            assertThat(changes).isEmpty();
        }

        @Test
        void shouldReturnEmptyListOfChangesWhenRespondentsAreEmpty() {
            List<ChangeOrganisationRequest> changes = service.getRepresentationChanges(emptyList(), emptyList());

            assertThat(changes).isEmpty();
        }

        @Test
        void shouldReturnEmptyListOfChangesWhenRespondentsHasNotChanged() {

            final Element<Respondent> respondent1 = element(Respondent.builder()
                .solicitor(RespondentSolicitor.builder()
                    .firstName("First")
                    .lastName("Respondent")
                    .build())
                .build());

            final Element<Respondent> respondent2 = element(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("Second")
                    .lastName("Respondent")
                    .build())
                .build());

            final Element<Respondent> respondent3 = element(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("Third")
                    .lastName("Respondent")
                    .build())
                .solicitor(RespondentSolicitor.builder()
                    .organisation(organisation("test"))
                    .build())
                .build());

            List<Element<Respondent>> newRespondents = List.of(respondent1, respondent2, respondent3);
            List<Element<Respondent>> oldRespondents = List.of(respondent1, respondent2, respondent3);

            List<ChangeOrganisationRequest> changes = service.getRepresentationChanges(newRespondents, oldRespondents);

            assertThat(changes).isEmpty();
        }

        @Test
        void shouldReturnEmptyListOfChangesWhenRespondentsRepresentationHasNotChanged() {

            final Element<Respondent> respondent1 = element(Respondent.builder()
                .solicitor(RespondentSolicitor.builder()
                    .firstName("First")
                    .lastName("Respondent")
                    .build())
                .build());

            final Element<Respondent> respondent2 = element(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("Second")
                    .lastName("Respondent")
                    .build())
                .solicitor(RespondentSolicitor.builder()
                    .organisation(organisation("test"))
                    .build())
                .build());

            final Element<Respondent> respondent2Updated = element(respondent2.getId(), respondent2.getValue()
                .toBuilder()
                .party(RespondentParty.builder()
                    .firstName("Second")
                    .firstName("Updated Respondents")
                    .build())
                .build());

            List<Element<Respondent>> oldRespondents = List.of(respondent1, respondent2);
            List<Element<Respondent>> newRespondents = List.of(respondent1, respondent2Updated);

            List<ChangeOrganisationRequest> changes = service.getRepresentationChanges(newRespondents, oldRespondents);

            assertThat(changes).isEmpty();
        }

        @Test
        void shouldReturnRequestToAddNewRepresentationWhenNewRespondentAdded() {

            final Organisation organisation = organisation("test");

            final Element<Respondent> existingRespondent = element(Respondent.builder()
                .solicitor(RespondentSolicitor.builder()
                    .firstName("First")
                    .lastName("Respondent")
                    .build())
                .build());

            final Element<Respondent> newRespondent = element(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("Second")
                    .lastName("Respondent")
                    .build())
                .solicitor(RespondentSolicitor.builder()
                    .organisation(organisation)
                    .build())
                .build());

            List<Element<Respondent>> oldRespondents = List.of(existingRespondent);
            List<Element<Respondent>> newRespondents = List.of(existingRespondent, newRespondent);

            List<ChangeOrganisationRequest> changes = service.getRepresentationChanges(newRespondents, oldRespondents);

            ChangeOrganisationRequest expected = ChangeOrganisationRequest.builder()
                .organisationToAdd(organisation)
                .approvalStatus(APPROVED)
                .requestTimestamp(time.now())
                .caseRoleId(caseRoleDynamicList("[SOLICITORB]"))
                .build();

            assertThat(changes).containsExactly(expected);
        }

        @Test
        void shouldReturnRequestToAddNewAndRemoveOldRepresentationWhenRespondentRepresentationChanged() {

            final Organisation newOrganisation = organisation("new");
            final Organisation oldOrganisation = organisation("old");

            final Element<Respondent> respondent1 = element(Respondent.builder()
                .solicitor(RespondentSolicitor.builder()
                    .firstName("First")
                    .lastName("Respondent")
                    .build())
                .build());

            final Element<Respondent> respondent2 = element(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("Second")
                    .lastName("Respondent")
                    .build())
                .solicitor(RespondentSolicitor.builder()
                    .organisation(oldOrganisation)
                    .build())
                .build());

            final Element<Respondent> respondent2Updated = element(respondent2.getId(),
                respondent2.getValue().toBuilder()
                    .solicitor(RespondentSolicitor.builder()
                        .organisation(newOrganisation)
                        .build())
                    .build());

            List<Element<Respondent>> oldRespondents = List.of(respondent1, respondent2);
            List<Element<Respondent>> newRespondents = List.of(respondent1, respondent2Updated);

            List<ChangeOrganisationRequest> changes = service.getRepresentationChanges(newRespondents, oldRespondents);

            ChangeOrganisationRequest expected = ChangeOrganisationRequest.builder()
                .organisationToAdd(newOrganisation)
                .organisationToRemove(oldOrganisation)
                .approvalStatus(APPROVED)
                .requestTimestamp(time.now())
                .caseRoleId(caseRoleDynamicList("[SOLICITORB]"))
                .build();

            assertThat(changes).containsExactly(expected);
        }


        @Test
        void shouldReturnMultiple() {

            final Organisation organisation1 = organisation("one");
            final Organisation organisation2 = organisation("two");
            final Organisation organisation3 = organisation("three");

            final Element<Respondent> respondent1 = element(Respondent.builder()
                .solicitor(RespondentSolicitor.builder()
                    .firstName("First")
                    .lastName("Respondent")
                    .build())
                .build());

            final Element<Respondent> respondent2 = element(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("Second")
                    .lastName("Respondent")
                    .build())
                .solicitor(RespondentSolicitor.builder()
                    .organisation(organisation1)
                    .build())
                .build());

            final Element<Respondent> respondent3 = element(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("Third")
                    .lastName("Respondent")
                    .build())
                .solicitor(RespondentSolicitor.builder()
                    .organisation(organisation2)
                    .build())
                .build());

            final Element<Respondent> respondent2Updated = element(respondent2.getId(),
                respondent2.getValue().toBuilder()
                    .solicitor(RespondentSolicitor.builder()
                        .organisation(organisation3)
                        .build())
                    .build());

            final Element<Respondent> respondent3Updated = element(respondent3.getId(),
                respondent3.getValue().toBuilder()
                    .solicitor(RespondentSolicitor.builder()
                        .organisation(organisation1)
                        .build())
                    .build());

            List<Element<Respondent>> oldRespondents = List.of(respondent1, respondent2, respondent3);
            List<Element<Respondent>> newRespondents = List.of(respondent1, respondent2Updated, respondent3Updated);

            List<ChangeOrganisationRequest> changes = service.getRepresentationChanges(newRespondents, oldRespondents);

            ChangeOrganisationRequest expected1 = ChangeOrganisationRequest.builder()
                .organisationToAdd(organisation3)
                .organisationToRemove(organisation1)
                .approvalStatus(APPROVED)
                .requestTimestamp(time.now())
                .caseRoleId(caseRoleDynamicList("[SOLICITORB]"))
                .build();

            ChangeOrganisationRequest expected2 = ChangeOrganisationRequest.builder()
                .organisationToAdd(organisation1)
                .organisationToRemove(organisation2)
                .approvalStatus(APPROVED)
                .requestTimestamp(time.now())
                .caseRoleId(caseRoleDynamicList("[SOLICITORC]"))
                .build();

            assertThat(changes).containsExactly(expected1, expected2);
        }

    }

}
