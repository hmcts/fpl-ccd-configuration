package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.aac.model.ChangeOrganisationRequest;
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
import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeAnswers;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static uk.gov.hmcts.reform.ccd.model.Organisation.organisation;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.caseRoleDynamicList;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class,
    NoticeOfChangeAnswersConverter.class,
    RespondentPolicyConverter.class,
    RespondentRepresentationService.class})
class RespondentRepresentationServiceTest {

    @Autowired
    private RespondentRepresentationService respondentPolicyService;

    @Test
    void shouldMapNoticeOfChangeAnswersAndRespondentOrganisationPoliciesFromCaseData() {
        RespondentParty respondentPartyOne = RespondentParty.builder()
            .firstName("Joe")
            .lastName("Bloggs")
            .build();

        RespondentParty respondentPartyTwo = RespondentParty.builder()
            .firstName("Sam")
            .lastName("Smith")
            .build();

        Organisation solicitorOrganisation = Organisation.builder()
            .organisationName("Summers Inc")
            .organisationID("12345")
            .build();

        RespondentSolicitor respondentSolicitor = RespondentSolicitor.builder()
            .firstName("Ben")
            .lastName("Summers")
            .email("bensummers@gmail.com")
            .organisation(solicitorOrganisation)
            .build();

        Respondent respondentOne = Respondent.builder()
            .party(respondentPartyOne)
            .legalRepresentation("Yes")
            .solicitor(respondentSolicitor)
            .build();

        Respondent respondentTwo = Respondent.builder().party(respondentPartyTwo).build();

        CaseData caseData = CaseData.builder()
            .respondents1(List.of(
                element(respondentOne),
                element(respondentTwo)))
            .applicants(List.of(element(Applicant.builder()
                .party(ApplicantParty.builder()
                    .organisationName("Test organisation")
                    .build())
                .build())))
            .build();


        Map<String, Object> data = respondentPolicyService.generateForSubmission(caseData);

        NoticeOfChangeAnswers expectedNoticeOfChangeAnswersOne = buildNoticeOfChangeAnswers(respondentPartyOne);
        NoticeOfChangeAnswers expectedNoticeOfChangeAnswersTwo = buildNoticeOfChangeAnswers(respondentPartyTwo);

        OrganisationPolicy expectedOrganisationPolicyOne = OrganisationPolicy.builder()
            .organisation(solicitorOrganisation)
            .orgPolicyCaseAssignedRole(SolicitorRole.SOLICITORA.getCaseRoleLabel())
            .build();

        Map<String, Object> expectedNoticeOfChangeAnswers = Map.of(
            "noticeOfChangeAnswers0", expectedNoticeOfChangeAnswersOne,
            "noticeOfChangeAnswers1", expectedNoticeOfChangeAnswersTwo);

        Map<String, Object> expectedRespondentPolicies = Map.of(
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

        Map<String, Object> expectedData = new HashMap<>();
        expectedData.putAll(expectedNoticeOfChangeAnswers);
        expectedData.putAll(expectedRespondentPolicies);

        assertThat(data).isEqualTo(expectedData);
    }

    @Test
    void shouldBuildRespondentPolicyFromEmptyCase() {
        CaseData caseData = CaseData.builder()
            .applicants(List.of(element(Applicant.builder()
                .party(ApplicantParty.builder()
                    .organisationName("Test organisation")
                    .build())
                .build())))
            .respondents1(emptyList())
            .build();

        Map<String, Object> data = respondentPolicyService.generateForSubmission(caseData);

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

        final UserDetails solicitorUser = UserDetails.builder()
            .forename("Tom")
            .surname("Wilson")
            .email("test@test.co.uk")
            .build();

        @Test
        void shouldThrowExceptionWhenOrganisationChangeRequestIsNotPresent() {
            final CaseData caseData = CaseData.builder()
                .build();

            assertThatThrownBy(() -> respondentPolicyService.updateRepresentation(caseData, solicitorUser))
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

            assertThatThrownBy(() -> respondentPolicyService.updateRepresentation(caseData, solicitorUser))
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

            assertThatThrownBy(() -> respondentPolicyService.updateRepresentation(caseData, solicitorUser))
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
                .build();

            final List<Element<Respondent>> updatedRespondents =
                respondentPolicyService.updateRepresentation(caseData, solicitorUser);

            final Element<Respondent> updatedRespondent =
                element(respondent1.getId(), respondent1.getValue().toBuilder()
                    .legalRepresentation("Yes")
                    .solicitor(RespondentSolicitor.builder()
                        .firstName("Tom")
                        .lastName("Wilson")
                        .email("test@test.co.uk")
                        .organisation(organisation)
                        .build())
                    .build());

            assertThat(updatedRespondents).containsExactly(updatedRespondent, respondent2);
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

            final Element<Respondent> respondent2 = element(Respondent.builder()
                .solicitor(RespondentSolicitor.builder()
                    .firstName("Emma")
                    .firstName("Green")
                    .email("emma.green@test.com")
                    .organisation(organisation("ORG2"))
                    .build())
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
                .build();

            final List<Element<Respondent>> updatedRespondents =
                respondentPolicyService.updateRepresentation(caseData, solicitorUser);

            final Element<Respondent> updatedRespondent =
                element(respondent2.getId(), respondent2.getValue().toBuilder()
                    .legalRepresentation("Yes")
                    .solicitor(RespondentSolicitor.builder()
                        .firstName("Tom")
                        .lastName("Wilson")
                        .email("test@test.co.uk")
                        .organisation(newOrganisation)
                        .build())
                    .build());

            assertThat(updatedRespondents).containsExactly(respondent1, updatedRespondent, respondent3);
        }
    }

    private NoticeOfChangeAnswers buildNoticeOfChangeAnswers(RespondentParty respondentParty) {
        return NoticeOfChangeAnswers.builder()
            .respondentFirstName(respondentParty.getFirstName())
            .respondentLastName(respondentParty.getLastName())
            .applicantName("Test organisation")
            .build();
    }

    private OrganisationPolicy buildOrganisationPolicy(SolicitorRole solicitorRole) {
        return OrganisationPolicy.builder()
            .organisation(Organisation.builder().build())
            .orgPolicyCaseAssignedRole(solicitorRole.getCaseRoleLabel())
            .build();
    }

}
