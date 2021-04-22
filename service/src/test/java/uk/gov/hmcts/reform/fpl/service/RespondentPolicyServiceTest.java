package uk.gov.hmcts.reform.fpl.service;

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
import uk.gov.hmcts.reform.fpl.model.RespondentPolicyData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeAnswers;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class,
    NoticeOfChangeAnswersConverter.class,
    RespondentPolicyConverter.class,
    RespondentPolicyService.class})
class RespondentPolicyServiceTest {

    @Autowired
    private RespondentPolicyService respondentPolicyService;

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

    @Test
    void shouldUpdateRespondentSolicitorWhenOrganisationChangeRequested() {

        Element<Respondent> respondent1 = element(Respondent.builder().build());

        Element<Respondent> respondent2 = element(Respondent.builder().build());

        Organisation organisation = Organisation.builder()
            .organisationID("test1")
            .build();

        DynamicListElement dynamicListElement = DynamicListElement.builder()
            .code("[SOLICITORA]")
            .label("Solicitor A")
            .build();

        DynamicList dynamicList = DynamicList.builder()
            .value(dynamicListElement)
            .listItems(List.of(dynamicListElement))
            .build();

        ChangeOrganisationRequest changeOrganisationRequest = ChangeOrganisationRequest.builder()
            .organisationToAdd(organisation)
            .caseRoleId(dynamicList)
            .build();

        CaseData caseData = CaseData.builder()
            .respondents1(List.of(respondent1, respondent2))
            .changeOrganisationRequestField(changeOrganisationRequest)
            .build();

        UserDetails userDetails = UserDetails.builder()
            .forename("Tom")
            .surname("Wilson")
            .email("test@test.co.uk")
            .build();

        List<Element<Respondent>> updatedRespondents =
            respondentPolicyService.updateNoticeOfChangeRepresentation(caseData, userDetails);

        Element<Respondent> expectedRespondent =
            element(respondent1.getId(), Respondent.builder()
                .legalRepresentation("Yes")
                .solicitor(RespondentSolicitor.builder()
                    .firstName("Tom")
                    .lastName("Wilson")
                    .email("test@test.co.uk")
                    .organisation(organisation)
                    .build())
                .build());

        assertThat(updatedRespondents).containsExactly(expectedRespondent, respondent2);
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

    private RespondentPolicyData buildRespondentPolicyData() {
        return RespondentPolicyData.builder()
            .respondentPolicy0(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole("[SOLICITORA]")
                .organisation(Organisation.builder()
                    .organisationID("000")
                    .organisationID("SA111")
                    .build())
                .build())
            .respondentPolicy1(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole("[SOLICITORB]")
                .organisation(Organisation.builder()
                    .organisationID("111")
                    .organisationID("BA123")
                    .build())
                .build())
            .respondentPolicy2(OrganisationPolicy.builder().build())
            .respondentPolicy3(OrganisationPolicy.builder().build())
            .respondentPolicy4(OrganisationPolicy.builder().build())
            .respondentPolicy5(OrganisationPolicy.builder().build())
            .respondentPolicy6(OrganisationPolicy.builder().build())
            .respondentPolicy7(OrganisationPolicy.builder().build())
            .respondentPolicy8(OrganisationPolicy.builder().build())
            .respondentPolicy9(OrganisationPolicy.builder().build())
            .build();
    }
}
