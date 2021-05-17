package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.components.NoticeOfChangeAnswersConverter;
import uk.gov.hmcts.reform.fpl.components.RespondentPolicyConverter;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.noc.ChangeOfRepresentation;
import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeAnswers;
import uk.gov.hmcts.reform.fpl.model.representative.ChangeOfRepresentationRequest;
import uk.gov.hmcts.reform.fpl.service.representative.ChangeOfRepresentationService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.model.Organisation.organisation;
import static uk.gov.hmcts.reform.fpl.model.noc.ChangeOfRepresentationMethod.NOC;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.caseRoleDynamicList;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class,
    NoticeOfChangeAnswersConverter.class,
    RespondentPolicyConverter.class,
    RespondentRepresentationService.class})
class RespondentRepresentationServiceTest {

    private static final List<Element<ChangeOfRepresentation>> UPDATED_CHANGE_OF_REPRESENTATIVES = List.of(element(mock(
        ChangeOfRepresentation.class)));
    private static final List<Element<ChangeOfRepresentation>> CHANGE_OF_REPRESENTATIVES = List.of(element(mock(
        ChangeOfRepresentation.class)));

    @Autowired
    private RespondentRepresentationService underTest;

    @MockBean
    private ChangeOfRepresentationService changeOfRepresentationService;

    @Test
    void shouldMapNoticeOfChangeAnswersAndRespondentOrganisationPoliciesFromCaseData() {
        final RespondentParty respondentPartyOne = RespondentParty.builder()
            .firstName("Joe")
            .lastName("Bloggs")
            .build();

        final RespondentParty respondentPartyTwo = RespondentParty.builder()
            .firstName("Sam")
            .lastName("Smith")
            .build();

        final Organisation solicitorOrganisation = Organisation.builder()
            .organisationName("Summers Inc")
            .organisationID("12345")
            .build();

        final RespondentSolicitor respondentSolicitor = RespondentSolicitor.builder()
            .firstName("Ben")
            .lastName("Summers")
            .email("bensummers@gmail.com")
            .organisation(solicitorOrganisation)
            .build();

        final Respondent respondentOne = Respondent.builder()
            .party(respondentPartyOne)
            .legalRepresentation("Yes")
            .solicitor(respondentSolicitor)
            .build();

        final Respondent respondentTwo = Respondent.builder().party(respondentPartyTwo).build();

        final CaseData caseData = CaseData.builder()
            .respondents1(List.of(
                element(respondentOne),
                element(respondentTwo)))
            .applicants(List.of(element(Applicant.builder()
                .party(ApplicantParty.builder()
                    .organisationName("Test organisation")
                    .build())
                .build())))
            .build();


        final Map<String, Object> data = underTest.generate(caseData);

        final NoticeOfChangeAnswers expectedNoticeOfChangeAnswersOne = buildNoticeOfChangeAnswers(respondentPartyOne);
        final NoticeOfChangeAnswers expectedNoticeOfChangeAnswersTwo = buildNoticeOfChangeAnswers(respondentPartyTwo);

        final OrganisationPolicy expectedOrganisationPolicyOne = OrganisationPolicy.builder()
            .organisation(solicitorOrganisation)
            .orgPolicyCaseAssignedRole(SolicitorRole.SOLICITORA.getCaseRoleLabel())
            .build();

        final Map<String, Object> expectedNoticeOfChangeAnswers = Map.of(
            "noticeOfChangeAnswers0", expectedNoticeOfChangeAnswersOne,
            "noticeOfChangeAnswers1", expectedNoticeOfChangeAnswersTwo);

        final Map<String, Object> expectedRespondentPolicies = Map.of(
            "respondentPolicy0", expectedOrganisationPolicyOne,
            "respondentPolicy1", buildOrganisationPolicy(SolicitorRole.SOLICITORB),
            "respondentPolicy2", buildOrganisationPolicy(SolicitorRole.SOLICITORC),
            "respondentPolicy3", buildOrganisationPolicy(SolicitorRole.SOLICITORD),
            "respondentPolicy4", buildOrganisationPolicy(SolicitorRole.SOLICITORE),
            "respondentPolicy5", buildOrganisationPolicy(SolicitorRole.SOLICITORF),
            "respondentPolicy6", buildOrganisationPolicy(SolicitorRole.SOLICITORG),
            "respondentPolicy7", buildOrganisationPolicy(SolicitorRole.SOLICITORH),
            "respondentPolicy8", buildOrganisationPolicy(SolicitorRole.SOLICITORI),
            "respondentPolicy9", buildOrganisationPolicy(SolicitorRole.SOLICITORJ));

        final Map<String, Object> expectedData = new HashMap<>();

        expectedData.putAll(expectedNoticeOfChangeAnswers);
        expectedData.putAll(expectedRespondentPolicies);

        assertThat(data).isEqualTo(expectedData);
    }

    @Test
    void shouldBuildRespondentPolicyFromEmptyCase() {
        final CaseData caseData = CaseData.builder()
            .applicants(List.of(element(Applicant.builder()
                .party(ApplicantParty.builder()
                    .organisationName("Test organisation")
                    .build())
                .build())))
            .respondents1(emptyList())
            .build();

        final Map<String, Object> data = underTest.generate(caseData);

        assertThat(data).isEqualTo(Map.of(
            "respondentPolicy0", buildOrganisationPolicy(SolicitorRole.SOLICITORA),
            "respondentPolicy1", buildOrganisationPolicy(SolicitorRole.SOLICITORB),
            "respondentPolicy2", buildOrganisationPolicy(SolicitorRole.SOLICITORC),
            "respondentPolicy3", buildOrganisationPolicy(SolicitorRole.SOLICITORD),
            "respondentPolicy4", buildOrganisationPolicy(SolicitorRole.SOLICITORE),
            "respondentPolicy5", buildOrganisationPolicy(SolicitorRole.SOLICITORF),
            "respondentPolicy6", buildOrganisationPolicy(SolicitorRole.SOLICITORG),
            "respondentPolicy7", buildOrganisationPolicy(SolicitorRole.SOLICITORH),
            "respondentPolicy8", buildOrganisationPolicy(SolicitorRole.SOLICITORI),
            "respondentPolicy9", buildOrganisationPolicy(SolicitorRole.SOLICITORJ)
        ));
    }

    @Nested
    class RepresentationUpdate {

        private static final String SOLICITOR_EMAIL = "test@test.co.uk";
        final UserDetails solicitorUser = UserDetails.builder()
            .forename("Tom")
            .surname("Wilson")
            .email("test@test.co.uk")
            .build();

        @Test
        void shouldThrowExceptionWhenOrganisationChangeRequestIsNotPresent() {
            final CaseData caseData = CaseData.builder()
                .build();

            assertThatThrownBy(() -> underTest.updateRepresentation(caseData, solicitorUser))
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

            assertThatThrownBy(() -> underTest.updateRepresentation(caseData, solicitorUser))
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

            assertThatThrownBy(() -> underTest.updateRepresentation(caseData, solicitorUser))
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

            when(changeOfRepresentationService.changeRepresentative(ChangeOfRepresentationRequest.builder()
                .method(NOC)
                .current(CHANGE_OF_REPRESENTATIVES)
                .respondent(respondent1.getValue())
                .removedRepresentative(null)
                .addedRepresentative(updatedRespondentSolicitor)
                .by(SOLICITOR_EMAIL)
                .build())).thenReturn(UPDATED_CHANGE_OF_REPRESENTATIVES);

            final Map<String, Object> actual =
                underTest.updateRepresentation(caseData, solicitorUser);

            final Element<Respondent> updatedRespondent =
                element(respondent1.getId(), respondent1.getValue().toBuilder()
                    .legalRepresentation("Yes")
                    .solicitor(updatedRespondentSolicitor)
                    .build());

            assertThat(actual).isEqualTo(Map.of(
                "respondents1", List.of(updatedRespondent, respondent2),
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

            RespondentSolicitor intialRespondentSolicitor = RespondentSolicitor.builder()
                .firstName("Emma")
                .lastName("Green")
                .email("emma.green@test.com")
                .organisation(organisation("ORG2"))
                .build();

            final Element<Respondent> respondent2 = element(Respondent.builder()
                .solicitor(intialRespondentSolicitor)
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

            when(changeOfRepresentationService.changeRepresentative(ChangeOfRepresentationRequest.builder()
                .method(NOC)
                .current(CHANGE_OF_REPRESENTATIVES)
                .respondent(respondent2.getValue())
                .removedRepresentative(intialRespondentSolicitor)
                .addedRepresentative(updatedRespondentSolicitor)
                .by(SOLICITOR_EMAIL)
                .build())).thenReturn(UPDATED_CHANGE_OF_REPRESENTATIVES);

            final Map<String, Object> actual =
                underTest.updateRepresentation(caseData, solicitorUser);

            final Element<Respondent> updatedRespondent =
                element(respondent2.getId(), respondent2.getValue().toBuilder()
                    .legalRepresentation("Yes")
                    .solicitor(updatedRespondentSolicitor)
                    .build());

            assertThat(actual).isEqualTo(Map.of(
                "respondents1", List.of(respondent1, updatedRespondent, respondent3),
                "changeOfRepresentatives", UPDATED_CHANGE_OF_REPRESENTATIVES
            ));

        }
    }

    private static NoticeOfChangeAnswers buildNoticeOfChangeAnswers(RespondentParty respondentParty) {
        return NoticeOfChangeAnswers.builder()
            .respondentFirstName(respondentParty.getFirstName())
            .respondentLastName(respondentParty.getLastName())
            .applicantName("Test organisation")
            .build();
    }

    private static OrganisationPolicy buildOrganisationPolicy(SolicitorRole solicitorRole) {
        return OrganisationPolicy.builder()
            .organisation(Organisation.builder().build())
            .orgPolicyCaseAssignedRole(solicitorRole.getCaseRoleLabel())
            .build();
    }

}
