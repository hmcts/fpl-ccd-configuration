package uk.gov.hmcts.reform.fpl.service;

import org.assertj.core.api.Assertions;
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
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeAnswers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORA;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORB;
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

    private final LocalDate respondentDOB = LocalDate.now();

    @Test
    void shouldMapNoticeOfChangeAnswersAndRespondentOrganisationPoliciesFromCaseData() {
        UUID respondentElementOneId = UUID.randomUUID();
        UUID respondentElementTwoId = UUID.randomUUID();

        RespondentParty respondentParty = buildRespondentParty();

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

        Respondent respondentTwo = Respondent.builder()
            .party(respondentParty)
            .build();

        List<Element<Respondent>> respondents = List.of(
            element(respondentElementOneId, respondentOne),
            element(respondentElementTwoId, respondentTwo));

        Map<String, Object> caseData = new HashMap<>(Map.of(
            "respondents1", respondents,
            "applicants", List.of(element(buildApplicant()))
        ));

        CaseDetails caseDetails = CaseDetails.builder().data(caseData).build();

        Map<String, Object> data = respondentPolicyService.generateForSubmission(caseDetails);

        NoticeOfChangeAnswers expectedNoticeOfChangeAnswersOne = buildNoticeOfChangeAnswers(0);
        NoticeOfChangeAnswers expectedNoticeOfChangeAnswersTwo = buildNoticeOfChangeAnswers(1);

        OrganisationPolicy expectedOrganisationPolicyOne = OrganisationPolicy.builder()
            .organisation(solicitorOrganisation)
            .orgPolicyCaseAssignedRole(SOLICITORA.getCaseRoleLabel())
            .build();

        OrganisationPolicy expectedOrganisationPolicyTwo = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole(SOLICITORB.getCaseRoleLabel())
            .build();

        List<Element<Respondent>> expectedUpdatedRespondents = List.of(
            element(respondentElementOneId, respondentOne.toBuilder().policyReference(0).build()),
            element(respondentElementTwoId, respondentTwo.toBuilder().policyReference(1).build()));

        Assertions.assertThat(data)
            .extracting(
                "respondents1",
                "noticeOfChangeAnswers0",
                "noticeOfChangeAnswers1",
                "respondentPolicy0",
                "respondentPolicy1")
            .containsExactly(
                expectedUpdatedRespondents,
                expectedNoticeOfChangeAnswersOne,
                expectedNoticeOfChangeAnswersTwo,
                expectedOrganisationPolicyOne,
                expectedOrganisationPolicyTwo);
    }

    @Test
    void shouldNotMapNoticeOfChangeAnswersAndRespondentOrganisationPoliciesFromCaseDataWhenApplicantDoNotExist() {
        Respondent respondentOne = Respondent.builder()
            .party(RespondentParty.builder().build())
            .legalRepresentation("Yes")
            .build();

        Respondent respondentTwo = Respondent.builder()
            .party(RespondentParty.builder().build())
            .build();

        List<Element<Respondent>> respondents = List.of(
            element(respondentOne),
            element(respondentTwo));

        CaseDetails caseDetails = CaseDetails.builder().data(Map.of(
            "respondents1", respondents,
            "familyManCaseNumber", "12345"
        )).build();

        Map<String, Object> data = respondentPolicyService.generateForSubmission(caseDetails);

        assertThat(data).isEmpty();
    }

    @Test
    void shouldNotMapNoticeOfChangeAnswersAndRespondentOrganisationPoliciesFromCaseDataWhenRespondentsDoNotExist() {
        CaseDetails caseDetails = CaseDetails.builder().data(Map.of(
            "applicants", List.of(element(buildApplicant())),
            "familyManCaseNumber", "12345"
        )).build();

        Map<String, Object> data = respondentPolicyService.generateForSubmission(caseDetails);

        assertThat(data).isEmpty();
    }

    private RespondentParty buildRespondentParty() {
        return RespondentParty.builder()
            .firstName("Joe")
            .lastName("Bloggs")
            .relationshipToChild("Father")
            .dateOfBirth(respondentDOB)
            .telephoneNumber(Telephone.builder()
                .contactDirection("By telephone")
                .telephoneNumber("02838882333")
                .telephoneUsageType("Personal home number")
                .build())
            .gender("Male")
            .placeOfBirth("Newry")
            .build();
    }

    private Applicant buildApplicant() {
        return Applicant.builder()
            .party(ApplicantParty.builder()
                .organisationName("Test organisation")
                .build())
            .build();
    }

    private NoticeOfChangeAnswers buildNoticeOfChangeAnswers(int policyId) {
        return NoticeOfChangeAnswers.builder()
            .respondentFirstName("Joe")
            .respondentLastName("Bloggs")
            .respondentDOB(respondentDOB)
            .applicantName("Test organisation")
            .policyReference(policyId)
            .build();
    }
}
