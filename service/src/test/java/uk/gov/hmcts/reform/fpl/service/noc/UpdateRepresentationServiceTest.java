package uk.gov.hmcts.reform.fpl.service.noc;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.noc.ChangeOfRepresentation;
import uk.gov.hmcts.reform.fpl.model.noc.ChangeOfRepresentationMethod;
import uk.gov.hmcts.reform.fpl.model.representative.ChangeOfRepresentationRequest;
import uk.gov.hmcts.reform.fpl.service.representative.ChangeOfRepresentationService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.model.Organisation.organisation;
import static uk.gov.hmcts.reform.fpl.model.noc.ChangeOfRepresentationMethod.NOC;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.caseRoleDynamicList;

class UpdateRepresentationServiceTest {

    private static final List<Element<ChangeOfRepresentation>> UPDATED_CHANGE_OF_REPRESENTATIVES = List.of(
        element(mock(ChangeOfRepresentation.class))
    );
    private static final List<Element<ChangeOfRepresentation>> CHANGE_OF_REPRESENTATIVES = List.of(
        element(mock(ChangeOfRepresentation.class))
    );
    private static final String SOLICITOR_EMAIL = "test@test.co.uk";
    private static final UserDetails USER = UserDetails.builder()
        .forename("Tom")
        .surname("Wilson")
        .email(SOLICITOR_EMAIL)
        .build();

    private final ChangeOfRepresentationService changeService = mock(ChangeOfRepresentationService.class);
    private final NoticeOfChangeUpdateAction updateAction = mock(NoticeOfChangeUpdateAction.class);

    private final UpdateRepresentationService underTest = new UpdateRepresentationService(
        changeService, List.of(updateAction)
    );

    @Test
    void shouldThrowExceptionWhenOrganisationChangeRequestIsNotPresent() {
        final CaseData caseData = CaseData.builder()
            .build();

        assertThatThrownBy(() -> underTest.updateRepresentation(caseData, USER))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Invalid or missing ChangeOrganisationRequest: null");

    }

    @Test
    void shouldThrowExceptionWhenRoleIsNotPresentInChangeRequest() {
        final ChangeOrganisationRequest changeOrganisationRequest = ChangeOrganisationRequest.builder()
            .organisationToAdd(organisation("Test"))
            .build();

        final CaseData caseData = CaseData.builder()
            .changeOrganisationRequestField(changeOrganisationRequest)
            .build();

        assertThatThrownBy(() -> underTest.updateRepresentation(caseData, USER))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Invalid or missing ChangeOrganisationRequest: " + changeOrganisationRequest);
    }

    @Test
    void shouldThrowExceptionWhenOrganisationToAddIsNotPresentInChangeRequest() {
        final ChangeOrganisationRequest changeOrganisationRequest = ChangeOrganisationRequest.builder()
            .caseRoleId(caseRoleDynamicList("[SOLICITOR1]"))
            .build();

        final CaseData caseData = CaseData.builder()
            .changeOrganisationRequestField(changeOrganisationRequest)
            .build();

        assertThatThrownBy(() -> underTest.updateRepresentation(caseData, USER))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Invalid or missing ChangeOrganisationRequest: " + changeOrganisationRequest);
    }

    @Test
    void shouldUpdateRespondentSolicitorWhenRepresentationAddedViaNoC() {
        final Element<Respondent> respondent1 = element(Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("John")
                .lastName("Smith")
                .build())
            .build());

        final Element<Respondent> respondent2 = element(Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Emma")
                .lastName("Green")
                .build())
            .build());

        final Organisation organisation = organisation("ORG");

        final ChangeOrganisationRequest changeOrganisationRequest = ChangeOrganisationRequest.builder()
            .organisationToAdd(organisation)
            .caseRoleId(caseRoleDynamicList("[SOLICITORA]"))
            .build();

        final CaseData caseData = CaseData.builder()
            .respondents1(List.of(respondent1, respondent2))
            .changeOrganisationRequestField(changeOrganisationRequest)
            .changeOfRepresentatives(CHANGE_OF_REPRESENTATIVES)
            .build();

        RespondentSolicitor updatedRespondentSolicitor = RespondentSolicitor.builder()
            .firstName("Tom")
            .lastName("Wilson")
            .email(SOLICITOR_EMAIL)
            .organisation(organisation)
            .build();

        when(changeService.changeRepresentative(
            ChangeOfRepresentationRequest.builder()
                .method(NOC)
                .current(CHANGE_OF_REPRESENTATIVES)
                .respondent(respondent1.getValue())
                .removedRepresentative(null)
                .addedRepresentative(updatedRespondentSolicitor)
                .by(SOLICITOR_EMAIL)
                .build()
        )).thenReturn(UPDATED_CHANGE_OF_REPRESENTATIVES);

        final Element<Respondent> updatedRespondent =
            element(respondent1.getId(), respondent1.getValue().toBuilder()
                .legalRepresentation("Yes")
                .solicitor(updatedRespondentSolicitor)
                .build());

        when(updateAction.accepts(SolicitorRole.Representing.RESPONDENT)).thenReturn(true);
        when(updateAction.applyUpdates(respondent1.getValue(), caseData, updatedRespondentSolicitor))
            .thenReturn(Map.of("respondents1", List.of(updatedRespondent, respondent2)));

        final Map<String, Object> actual = underTest.updateRepresentation(caseData, USER);

        assertThat(actual).isEqualTo(Map.of(
            "respondents1", List.of(updatedRespondent, respondent2),
            "changeOfRepresentatives", UPDATED_CHANGE_OF_REPRESENTATIVES
        ));
    }

    @Test
    void shouldUpdateChildSolicitorWhenRepresentationAddedViaNoC() {
        final Element<Child> child1 = element(Child.builder()
            .party(ChildParty.builder()
                .firstName("John")
                .lastName("Smith")
                .build())
            .build());

        final Element<Child> child2 = element(Child.builder()
            .party(ChildParty.builder()
                .firstName("Emma")
                .lastName("Green")
                .build())
            .build());

        final Organisation organisation = organisation("ORG");

        final ChangeOrganisationRequest changeOrganisationRequest = ChangeOrganisationRequest.builder()
            .organisationToAdd(organisation)
            .caseRoleId(caseRoleDynamicList("[CHILDSOLICITORA]"))
            .build();

        final CaseData caseData = CaseData.builder()
            .children1(List.of(child1, child2))
            .changeOrganisationRequestField(changeOrganisationRequest)
            .changeOfRepresentatives(CHANGE_OF_REPRESENTATIVES)
            .build();

        RespondentSolicitor updatedChildSolicitor = RespondentSolicitor.builder()
            .firstName("Tom")
            .lastName("Wilson")
            .email(SOLICITOR_EMAIL)
            .organisation(organisation)
            .build();

        when(changeService.changeRepresentative(
            ChangeOfRepresentationRequest.builder()
                .method(NOC)
                .current(CHANGE_OF_REPRESENTATIVES)
                .child(child1.getValue())
                .removedRepresentative(null)
                .addedRepresentative(updatedChildSolicitor)
                .by(SOLICITOR_EMAIL)
                .build()
        )).thenReturn(UPDATED_CHANGE_OF_REPRESENTATIVES);

        final Element<Child> updatedChild =
            element(child1.getId(), child1.getValue().toBuilder()
                .solicitor(updatedChildSolicitor)
                .build());

        when(updateAction.accepts(SolicitorRole.Representing.CHILD)).thenReturn(true);
        when(updateAction.applyUpdates(child1.getValue(), caseData, updatedChildSolicitor))
            .thenReturn(Map.of("children1", List.of(updatedChild, child2)));

        final Map<String, Object> actual = underTest.updateRepresentation(caseData, USER);

        assertThat(actual).isEqualTo(Map.of(
            "children1", List.of(updatedChild, child2),
            "changeOfRepresentatives", UPDATED_CHANGE_OF_REPRESENTATIVES
        ));
    }

    @Test
    void shouldUpdateRespondentSolicitorWhenRepresentationUpdatedViaNoC() {
        final Element<Respondent> respondent1 = element(Respondent.builder()
            .solicitor(RespondentSolicitor.builder()
                .firstName("John")
                .firstName("Smith")
                .email("john.smith@test.com")
                .organisation(organisation("ORG1"))
                .build())
            .build());

        RespondentSolicitor initialRespondentSolicitor = RespondentSolicitor.builder()
            .firstName("Emma")
            .lastName("Green")
            .email("emma.green@test.com")
            .organisation(organisation("ORG2"))
            .build();

        final Element<Respondent> respondent2 = element(Respondent.builder()
            .solicitor(initialRespondentSolicitor)
            .build());

        final Element<Respondent> respondent3 = element(Respondent.builder().build());

        final Organisation newOrganisation = organisation("ORG3");

        final ChangeOrganisationRequest changeOrganisationRequest = ChangeOrganisationRequest.builder()
            .organisationToAdd(newOrganisation)
            .caseRoleId(caseRoleDynamicList("[SOLICITORB]"))
            .build();

        final CaseData caseData = CaseData.builder()
            .respondents1(List.of(respondent1, respondent2, respondent3))
            .changeOrganisationRequestField(changeOrganisationRequest)
            .changeOfRepresentatives(CHANGE_OF_REPRESENTATIVES)
            .build();

        RespondentSolicitor updatedRespondentSolicitor = RespondentSolicitor.builder()
            .firstName("Tom")
            .lastName("Wilson")
            .email(SOLICITOR_EMAIL)
            .organisation(newOrganisation)
            .build();

        when(changeService.changeRepresentative(
            ChangeOfRepresentationRequest.builder()
                .method(NOC)
                .current(CHANGE_OF_REPRESENTATIVES)
                .respondent(respondent2.getValue())
                .removedRepresentative(initialRespondentSolicitor)
                .addedRepresentative(updatedRespondentSolicitor)
                .by(SOLICITOR_EMAIL)
                .build()
        )).thenReturn(UPDATED_CHANGE_OF_REPRESENTATIVES);

        final Element<Respondent> updatedRespondent =
            element(respondent2.getId(), respondent2.getValue().toBuilder()
                .legalRepresentation("Yes")
                .solicitor(updatedRespondentSolicitor)
                .build());

        when(updateAction.accepts(SolicitorRole.Representing.RESPONDENT)).thenReturn(true);
        when(updateAction.applyUpdates(respondent2.getValue(), caseData, updatedRespondentSolicitor))
            .thenReturn(Map.of("respondents1", List.of(respondent1, updatedRespondent, respondent3)));

        final Map<String, Object> actual = underTest.updateRepresentation(caseData, USER);

        assertThat(actual).isEqualTo(Map.of(
            "respondents1", List.of(respondent1, updatedRespondent, respondent3),
            "changeOfRepresentatives", UPDATED_CHANGE_OF_REPRESENTATIVES
        ));
    }

    @Test
    void shouldUpdateRepresentationForThirdPartyOutsourcing() {
        Organisation oldOrg = Organisation.builder().organisationID("Test123").build();
        Organisation newOrg = Organisation.builder().organisationID("Test456").build();

        RespondentSolicitor initialSolicitor = RespondentSolicitor.builder()
            .organisation(oldOrg)
            .build();

        RespondentSolicitor newSolicitor = RespondentSolicitor.builder()
            .firstName("Tom")
            .lastName("Wilson")
            .email(SOLICITOR_EMAIL)
            .organisation(newOrg)
            .build();

        final CaseData caseData = CaseData.builder()
            .outsourcingPolicy(OrganisationPolicy.builder()
                .organisation(oldOrg)
                .orgPolicyCaseAssignedRole("[EPSMANAGING]")
                .build())
            .changeOrganisationRequestField(ChangeOrganisationRequest.builder()
                .organisationToAdd(newOrg)
                .caseRoleId(caseRoleDynamicList("[EPSMANAGING]"))
                .build())
            .changeOfRepresentatives(CHANGE_OF_REPRESENTATIVES)
            .build();

        when(changeService.changeRepresentative(
            ChangeOfRepresentationRequest.builder()
                .method(NOC)
                .by(SOLICITOR_EMAIL)
                .current(CHANGE_OF_REPRESENTATIVES)
                .addedRepresentative(newSolicitor)
                .removedRepresentative(initialSolicitor)
                .build()
        )).thenReturn(UPDATED_CHANGE_OF_REPRESENTATIVES);

        final Map<String, Object> actual = underTest.updateRepresentationThirdPartyOutsourcing(caseData, USER);

        assertThat(actual).isEqualTo(Map.of(
            "changeOfRepresentatives", UPDATED_CHANGE_OF_REPRESENTATIVES
        ));
    }
}
