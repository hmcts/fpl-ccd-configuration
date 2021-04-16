package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.components.NoticeOfChangeAnswersConverter;
import uk.gov.hmcts.reform.fpl.components.RespondentPolicyConverter;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeAnswers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        RespondentParty respondentParty = RespondentParty.builder()
            .firstName("Joe")
            .lastName("Bloggs")
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
            .party(respondentParty)
            .legalRepresentation("Yes")
            .solicitor(respondentSolicitor)
            .build();

        Respondent respondentTwo = Respondent.builder().party(respondentParty).build();

        Map<String, Object> caseData = new HashMap<>(Map.of(
            "respondents1", List.of(
                element(respondentOne),
                element(respondentTwo)),
            "applicants", List.of(element(Applicant.builder()
                .party(ApplicantParty.builder()
                    .organisationName("Test organisation")
                    .build())
                .build()))
        ));

        CaseDetails caseDetails = CaseDetails.builder().data(caseData).build();

        Map<String, Object> data = respondentPolicyService.generateForSubmission(caseDetails);

        NoticeOfChangeAnswers expectedNoticeOfChangeAnswersOne = buildNoticeOfChangeAnswers(0);
        NoticeOfChangeAnswers expectedNoticeOfChangeAnswersTwo = buildNoticeOfChangeAnswers(1);

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

    private NoticeOfChangeAnswers buildNoticeOfChangeAnswers(int policyId) {
        return NoticeOfChangeAnswers.builder()
            .respondentFirstName("Joe")
            .respondentLastName("Bloggs")
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
