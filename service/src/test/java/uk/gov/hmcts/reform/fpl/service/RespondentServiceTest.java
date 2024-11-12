package uk.gov.hmcts.reform.fpl.service;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.enums.IsAddressKnowType;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.UnregisteredOrganisation;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.fpl.utils.RespondentsTestHelper;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.ccd.model.ChangeOrganisationApprovalStatus.APPROVED;
import static uk.gov.hmcts.reform.ccd.model.Organisation.organisation;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.RespondentsTestHelper.respondent;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.caseRoleDynamicList;

@ExtendWith(MockitoExtension.class)
class RespondentServiceTest {

    @Spy
    private Time time = new FixedTimeConfiguration().stoppedTime();

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
            List<Element<Respondent>> respondents = RespondentsTestHelper.respondents();

            String result = service.buildRespondentLabel(respondents);

            assertThat(result).isEqualTo("Respondent 1 - James Daniels\n"
                + "Respondent 2 - Bob Martyn\n"
                + "Respondent 3 - Rachel Daniels\n");
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

        List<Element<Respondent>> updatedRespondents = service.consolidateAndRemoveHiddenFields(respondents);
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

        List<Element<Respondent>> updatedRespondents = service.consolidateAndRemoveHiddenFields(respondents);
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

        List<Element<Respondent>> updatedRespondents = service.consolidateAndRemoveHiddenFields(respondents);
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

        List<Element<Respondent>> updatedRespondents = service.consolidateAndRemoveHiddenFields(respondents);
        assertThat(updatedRespondents.get(0).getValue().getSolicitor().getRegionalOfficeAddress()).isNull();
    }

    @Test
    void shouldSetConfidentWhenLiveInRefugeIsSelected() {
        List<Element<Respondent>> respondents = List.of(element(Respondent.builder()
            .party(RespondentParty.builder().addressKnow(IsAddressKnowType.LIVE_IN_REFUGE.getValue()).build())
            .build()));

        List<Element<Respondent>> updatedRespondents = service.consolidateAndRemoveHiddenFields(respondents);
        assertThat(updatedRespondents.get(0).getValue().getParty().getContactDetailsHidden())
            .isEqualTo(YES.getValue());
    }

    @Test
    void shouldReturnRespondentsWithRegisteredSolicitors() {
        RespondentSolicitor registeredSolicitor = RespondentSolicitor.builder()
            .firstName("Steven")
            .organisation(Organisation.builder().organisationID("Organisation ID").build())
            .build();

        RespondentSolicitor unregisteredSolicitor = RespondentSolicitor.builder()
            .firstName("Andrew")
            .unregisteredOrganisation(UnregisteredOrganisation.builder().name("unregistered org").build())
            .build();

        Respondent respondentWithRegisteredSolicitor = Respondent.builder()
            .legalRepresentation(YES.getValue())
            .solicitor(registeredSolicitor)
            .build();

        List<Element<Respondent>> respondents = List.of(element(respondentWithRegisteredSolicitor),
            element(Respondent.builder()
                .legalRepresentation(YES.getValue())
                .solicitor(unregisteredSolicitor)
                .build()));

        assertThat(service.getRespondentsWithRegisteredSolicitors(respondents))
            .containsOnly(respondentWithRegisteredSolicitor);
    }

    @Test
    void shouldReturnRespondentsWithUnregisteredSolicitors() {
        RespondentSolicitor unregisteredSolicitor = RespondentSolicitor.builder()
            .firstName("Steven")
            .unregisteredOrganisation(UnregisteredOrganisation.builder().name("unregistered org").build())
            .build();

        RespondentSolicitor registeredSolicitor = RespondentSolicitor.builder()
            .firstName("Andrew")
            .organisation(Organisation.builder().organisationID("Organisation ID").build())
            .build();

        Respondent respondentWithUnregisteredSolicitor = Respondent.builder()
            .legalRepresentation(YES.getValue())
            .solicitor(unregisteredSolicitor)
            .build();

        List<Element<Respondent>> respondents = List.of(element(respondentWithUnregisteredSolicitor),
            element(Respondent.builder()
                .legalRepresentation(YES.getValue())
                .solicitor(registeredSolicitor)
                .build()));

        assertThat(service.getRespondentsWithUnregisteredSolicitors(respondents))
            .containsOnly(respondentWithUnregisteredSolicitor);
    }

    @Test
    void shouldReturnEmptyWhenNoLegalRepresentation() {
        List<Element<Respondent>> respondents = List.of(element(Respondent.builder()
                .legalRepresentation(NO.getValue())
                .build()),
            element(Respondent.builder()
                .legalRepresentation(NO.getValue())
                .build()));

        List<Respondent> registeredSolicitors = service.getRespondentsWithRegisteredSolicitors(respondents);
        List<Respondent> unregisteredSolicitors = service.getRespondentsWithUnregisteredSolicitors(respondents);

        assertThat(registeredSolicitors).isEmpty();
        assertThat(unregisteredSolicitors).isEmpty();
    }

    private Respondent buildRespondent(String legalRepresentation, String email) {
        return buildRespondent(legalRepresentation, email, null);
    }

    private Respondent buildRespondent(String legalRepresentation, String email, String telephoneNumber) {
        return Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Test respondent")
                .build())
            .legalRepresentation(legalRepresentation)
            .solicitor(buildRespondentSolicitor(email, telephoneNumber))
            .build();
    }

    private RespondentSolicitor buildRespondentSolicitor(String email, String telephoneNumber) {
        if (isBlank(telephoneNumber)) {
            return RespondentSolicitor.builder()
                .firstName("Test respondent solicitor")
                .email(email)
                .telephoneNumber(Telephone.builder().build())
                .build();
        } else {
            return RespondentSolicitor.builder()
                .firstName("Test respondent solicitor")
                .email(email)
                .telephoneNumber(Telephone.builder().telephoneNumber(telephoneNumber).build())
                .build();
        }
    }

    @Nested
    @SuppressWarnings("unchecked")
    class RepresentationChanges {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnEmptyListOfChangesWhenRespondentsNotPresent(List<Element<Respondent>> respondents) {
            List<ChangeOrganisationRequest> changes = service.getRepresentationChanges((List) respondents,
                (List) respondents,
                SolicitorRole.Representing.RESPONDENT);

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

            List<ChangeOrganisationRequest> changes = service.getRepresentationChanges((List) newRespondents,
                (List) oldRespondents,
                SolicitorRole.Representing.RESPONDENT);

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

            List<ChangeOrganisationRequest> changes = service.getRepresentationChanges((List) newRespondents,
                (List) oldRespondents,
                SolicitorRole.Representing.RESPONDENT);

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

            List<ChangeOrganisationRequest> changes = service.getRepresentationChanges((List) newRespondents,
                (List) oldRespondents,
                SolicitorRole.Representing.RESPONDENT);

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

            List<ChangeOrganisationRequest> changes = service.getRepresentationChanges((List) newRespondents,
                (List) oldRespondents,
                SolicitorRole.Representing.RESPONDENT);

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

            List<ChangeOrganisationRequest> changes = service.getRepresentationChanges((List) newRespondents,
                (List) oldRespondents,
                SolicitorRole.Representing.RESPONDENT);

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

    @Test
    void shouldSelectAllWhenAskedToSelectAll() {
        Respondent firstRespondent = Respondent.builder().legalRepresentation("0").build();
        Respondent secondRespondent = Respondent.builder().legalRepresentation("1").build();

        List<Element<Respondent>> allRespondents = List.of(element(firstRespondent), element(secondRespondent));
        Selector selector = Selector.builder().selected(List.of(0, 1)).build().setNumberOfOptions(2);

        List<Element<Respondent>> selected = service.getSelectedRespondents(allRespondents, selector, "Yes");

        assertThat(selected).isEqualTo(allRespondents);
    }

    @Test
    void shouldSelectSomeWhenAskedToNotSelectAll() {
        Element<Respondent> firstRespondent = element(Respondent.builder().legalRepresentation("0").build());
        Element<Respondent> secondRespondent = element(Respondent.builder().legalRepresentation("1").build());

        List<Element<Respondent>> allRespondents = List.of(firstRespondent, secondRespondent);
        List<Element<Respondent>> expected = List.of(firstRespondent);
        Selector selector = Selector.builder().selected(List.of(0)).build().setNumberOfOptions(2);

        List<Element<Respondent>> selected = service.getSelectedRespondents(allRespondents, selector, "No");

        assertThat(selected).isEqualTo(expected);
    }

    @Test
    void shouldReturnTrueWhenAddressChange() {
        List<Element<Respondent>> respondentsBefore = wrapElements(Respondent.builder()
            .party(RespondentParty.builder().address(Address.builder().addressLine1("33 Testing Court")
                .addressLine2("Testing").postcode("XX1 BBB").build()).build()).build());
        List<Element<Respondent>> respondentsAfter = wrapElements(Respondent.builder()
            .party(RespondentParty.builder().address(Address.builder().addressLine1("90 Testing Court")
                .addressLine2("Testing").postcode("KK1 BBB").build()).build()).build());
        assertThat(service.hasAddressChange(Collections.unmodifiableList(respondentsAfter),
            Collections.unmodifiableList(respondentsBefore))).isTrue();
    }

    @Test
    void shouldReturnFalseWhenAddressNoChange() {
        List<Element<Respondent>> respondentsBefore = wrapElements(Respondent.builder()
            .party(RespondentParty.builder().address(Address.builder().addressLine1("33 Testing Court")
                .addressLine2("Testing").postcode("XX1 BBB").build()).build()).build());
        List<Element<Respondent>> respondentsAfter = wrapElements(Respondent.builder()
            .party(RespondentParty.builder().address(Address.builder().addressLine1("33 Testing Court")
                .addressLine2("Testing").postcode("XX1 BBB").build()).build()).build());
        assertThat(service.hasAddressChange(Collections.unmodifiableList(respondentsAfter),
            Collections.unmodifiableList(respondentsBefore))).isFalse();
    }

    @Test
    void shouldReturnTrueWhenAfterAddressIsNull() {
        List<Element<Respondent>> respondentsBefore = wrapElements(Respondent.builder()
            .party(RespondentParty.builder().address(Address.builder().addressLine1("33 Testing Court")
                .addressLine2("Testing").postcode("XX1 BBB").build()).build()).build());
        List<Element<Respondent>> respondentsAfter = wrapElements(Respondent.builder()
            .party(RespondentParty.builder().address(null).build()).build());
        assertThat(service.hasAddressChange(Collections.unmodifiableList(respondentsAfter),
            Collections.unmodifiableList(respondentsBefore))).isTrue();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldTransformOtherToRespondent(boolean hasRepresentative) {
        UUID id = UUID.randomUUID();
        UUID representativeId = UUID.randomUUID();

        Other other = Other.builder()
            .address(Address.builder()
                .postcode("XXX YYY")
                .addressLine1("addressLine1")
                .build())
            .addressKnow("Yes")
            .addressNotKnowReason("addressNotKnowReason")
            .detailsHidden("Yes")
            .detailsHiddenReason("detailsHiddenReason")
            .dateOfBirth("1989-06-04")
            .name("Other Name")
            .gender("Male")
            .genderIdentification("genderIdentification")
            .litigationIssuesDetails("litigationIssuesDetails")
            .litigationIssues("litigationIssues")
            .childInformation("childInformation")
            .telephone("7776894894")
            .build();
        if (hasRepresentative) {
            other.addRepresentative(id, representativeId);
        }

        Respondent expected = Respondent.builder()
            .representedBy(hasRepresentative ? List.of(element(id, representativeId)) : Lists.emptyList())
            .party(RespondentParty.builder()
                .address(Address.builder()
                    .postcode("XXX YYY")
                    .addressLine1("addressLine1")
                    .build())
                .addressKnow("Yes")
                .addressNotKnowReason("addressNotKnowReason")
                .contactDetailsHiddenReason("detailsHiddenReason")
                .contactDetailsHidden("Yes")
                .dateOfBirth(LocalDate.of(1989, Month.JUNE, 4))
                .firstName("Other Name")
                .gender("Male")
                .genderIdentification("genderIdentification")
                .litigationIssuesDetails("litigationIssuesDetails")
                .litigationIssues("litigationIssues")
                .relationshipToChild("childInformation")
                .telephoneNumber(Telephone.builder().telephoneNumber("7776894894").build())
                .build())
            .build();
        assertThat(service.transformOtherToRespondent(other)).isEqualTo(expected);
    }

    @Test
    void shouldGetRespondentSolicitorTelephones() {
        List<Respondent> respondents = List.of(buildRespondent(YES.getValue(), "email-1@test.com", null),
            buildRespondent(YES.getValue(), "email-2@test.com", "1234 567 890"));

        List<String> respondentSolicitorTelephones = service.getRespondentSolicitorTelephones(respondents);

        assertThat(respondentSolicitorTelephones).containsExactlyInAnyOrder("1234 567 890");
    }

}
