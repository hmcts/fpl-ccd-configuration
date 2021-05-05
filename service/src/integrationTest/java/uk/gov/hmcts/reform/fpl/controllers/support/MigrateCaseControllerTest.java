package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.NoticeOfChangeAnswersData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentPolicyData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.UnregisteredOrganisation;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeAnswers;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractCallbackTest {
    MigrateCaseControllerTest() {
        super("migrate-case");
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Fpla2961 {
        Long caseId = 1111L;
        String migrationId = "FPLA-2961";
        NoticeOfChangeAnswersData emptyNoticeOfChangeAnswersData = NoticeOfChangeAnswersData.builder().build();
        RespondentPolicyData emptyRespondentPolicyData = RespondentPolicyData.builder().build();

        private Stream<Arguments> invalidStates() {
            return Stream.of(
                Arguments.of(State.OPEN),
                Arguments.of(State.CLOSED),
                Arguments.of(State.DELETED)
            );
        }

        @Test
        void shouldGenerateRespondentPoliciesAndNoticeOfChangeAnswersFromFullyPopulatedCaseData() {
            List<Element<Applicant>> applicants = buildApplicants();

            Organisation respondentTwoOrganisation = Organisation.builder()
                .organisationID("SA123")
                .organisationName("Private solicitor")
                .build();

            List<Element<Respondent>> respondents = List.of(
                element(Respondent.builder()
                    .party(RespondentParty.builder()
                        .firstName("Sarah")
                        .lastName("Simpson")
                        .build())
                    .build()),
                element(Respondent.builder()
                    .solicitor(RespondentSolicitor.builder()
                        .organisation(respondentTwoOrganisation)
                        .build())
                    .party(RespondentParty.builder()
                        .firstName("Mark")
                        .lastName("Watson")
                        .build())
                    .build()),
                element(Respondent.builder()
                    .solicitor(RespondentSolicitor.builder()
                        .unregisteredOrganisation(UnregisteredOrganisation.builder()
                            .name("Some address")
                            .build())
                        .build())
                    .party(RespondentParty.builder()
                        .firstName("Gareth")
                        .lastName("Simmons")
                        .build())
                    .build()));

            CaseDetails caseDetails = caseDetails(respondents, applicants, migrationId);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            NoticeOfChangeAnswersData expectedNoticeOfChangeAnswers = NoticeOfChangeAnswersData.builder()
                .noticeOfChangeAnswers0(NoticeOfChangeAnswers.builder()
                    .applicantName("Swansea City Council")
                    .respondentFirstName("Sarah")
                    .respondentLastName("Simpson")
                    .build())
                .noticeOfChangeAnswers1(NoticeOfChangeAnswers.builder()
                    .applicantName("Swansea City Council")
                    .respondentFirstName("Mark")
                    .respondentLastName("Watson")
                    .build())
                .noticeOfChangeAnswers2(NoticeOfChangeAnswers.builder()
                    .applicantName("Swansea City Council")
                    .respondentFirstName("Gareth")
                    .respondentLastName("Simmons")
                    .build())
                .build();

            RespondentPolicyData expectedRespondentPolicyData = RespondentPolicyData.builder()
                .respondentPolicy0(buildOrganisationPolicy(SolicitorRole.SOLICITORA))
                .respondentPolicy1(OrganisationPolicy.builder()
                    .organisation(respondentTwoOrganisation)
                    .orgPolicyCaseAssignedRole(SolicitorRole.SOLICITORB.getCaseRoleLabel())
                    .build())
                .respondentPolicy2(buildOrganisationPolicy(SolicitorRole.SOLICITORC))
                .respondentPolicy3(buildOrganisationPolicy(SolicitorRole.SOLICITORD))
                .respondentPolicy4(buildOrganisationPolicy(SolicitorRole.SOLICITORE))
                .respondentPolicy5(buildOrganisationPolicy(SolicitorRole.SOLICITORF))
                .respondentPolicy6(buildOrganisationPolicy(SolicitorRole.SOLICITORG))
                .respondentPolicy7(buildOrganisationPolicy(SolicitorRole.SOLICITORH))
                .respondentPolicy8(buildOrganisationPolicy(SolicitorRole.SOLICITORI))
                .respondentPolicy9(buildOrganisationPolicy(SolicitorRole.SOLICITORJ))
                .build();

            assertThat(extractedCaseData.getNoticeOfChangeAnswersData()).isEqualTo(expectedNoticeOfChangeAnswers);
            assertThat(extractedCaseData.getRespondentPolicyData()).isEqualTo(expectedRespondentPolicyData);
        }

        @Test
        void shouldGenerateRespondentPoliciesAndNoticeOfChangeAnswersFromPartiallyPopulatedCaseData() {
            List<Element<Applicant>> applicants = List.of(
                element(Applicant.builder()
                    .party(ApplicantParty.builder()
                        .build())
                    .build()));

            List<Element<Respondent>> respondents = List.of(
                element(Respondent.builder()
                    .party(RespondentParty.builder()
                        .lastName("Simpson")
                        .build())
                    .build()),
                element(Respondent.builder()
                    .solicitor(RespondentSolicitor.builder()
                        .organisation(Organisation.builder().build())
                        .build())
                    .party(RespondentParty.builder()
                        .firstName("Mark")
                        .build())
                    .build()),
                element(Respondent.builder()
                    .solicitor(RespondentSolicitor.builder()
                        .unregisteredOrganisation(UnregisteredOrganisation.builder()
                            .name("Some address")
                            .build())
                        .build())
                    .party(RespondentParty.builder()
                        .build())
                    .build()));

            CaseDetails caseDetails = caseDetails(respondents, applicants, migrationId);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            NoticeOfChangeAnswersData expectedNoticeOfChangeAnswers = NoticeOfChangeAnswersData.builder()
                .noticeOfChangeAnswers0(NoticeOfChangeAnswers.builder()
                    .applicantName(null)
                    .respondentFirstName(null)
                    .respondentLastName("Simpson")
                    .build())
                .noticeOfChangeAnswers1(NoticeOfChangeAnswers.builder()
                    .applicantName(null)
                    .respondentFirstName("Mark")
                    .respondentLastName(null)
                    .build())
                .noticeOfChangeAnswers2(NoticeOfChangeAnswers.builder()
                    .applicantName(null)
                    .respondentFirstName(null)
                    .respondentLastName(null)
                    .build())
                .build();

            RespondentPolicyData expectedRespondentPolicyData = RespondentPolicyData.builder()
                .respondentPolicy0(buildOrganisationPolicy(SolicitorRole.SOLICITORA))
                .respondentPolicy1(buildOrganisationPolicy(SolicitorRole.SOLICITORB))
                .respondentPolicy2(buildOrganisationPolicy(SolicitorRole.SOLICITORC))
                .respondentPolicy3(buildOrganisationPolicy(SolicitorRole.SOLICITORD))
                .respondentPolicy4(buildOrganisationPolicy(SolicitorRole.SOLICITORE))
                .respondentPolicy5(buildOrganisationPolicy(SolicitorRole.SOLICITORF))
                .respondentPolicy6(buildOrganisationPolicy(SolicitorRole.SOLICITORG))
                .respondentPolicy7(buildOrganisationPolicy(SolicitorRole.SOLICITORH))
                .respondentPolicy8(buildOrganisationPolicy(SolicitorRole.SOLICITORI))
                .respondentPolicy9(buildOrganisationPolicy(SolicitorRole.SOLICITORJ))
                .build();

            assertThat(extractedCaseData.getNoticeOfChangeAnswersData()).isEqualTo(expectedNoticeOfChangeAnswers);
            assertThat(extractedCaseData.getRespondentPolicyData()).isEqualTo(expectedRespondentPolicyData);
        }

        @Test
        void shouldNotMigrateCaseIfMigrationIdIsIncorrect() {
            String incorrectMigrationId = "FPLA-1111";
            List<Element<Applicant>> applicants = buildApplicants();

            List<Element<Respondent>> respondents = List.of(
                element(Respondent.builder()
                    .party(RespondentParty.builder()
                        .lastName("Simpson")
                        .build())
                    .build()));

            CaseDetails caseDetails = caseDetails(respondents, applicants, incorrectMigrationId);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getRespondents1()).isEqualTo(respondents);
            assertThat(extractedCaseData.getApplicants()).isEqualTo(applicants);
            assertThat(extractedCaseData.getRespondentPolicyData()).isEqualTo(emptyRespondentPolicyData);
            assertThat(extractedCaseData.getNoticeOfChangeAnswersData()).isEqualTo(emptyNoticeOfChangeAnswersData);
        }

        @ParameterizedTest
        @MethodSource("invalidStates")
        void shouldThrowAnExceptionIfStateIsUnsupported(State caseState) {
            List<Element<Applicant>> applicants = buildApplicants();

            List<Element<Respondent>> respondents = List.of(
                element(Respondent.builder()
                    .party(RespondentParty.builder()
                        .lastName("Simpson")
                        .build())
                    .build()));

            CaseDetails caseDetails = caseDetails(respondents, applicants, migrationId);
            caseDetails.setState(caseState.getValue());

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format("Migration failed on case %s: Unexpected migration", caseId));
        }

        @Test
        void shouldThrowAnExceptionIfCaseContainsNoticeOfChangeAnswerData() {
            List<Element<Applicant>> applicants = buildApplicants();

            List<Element<Respondent>> respondents = List.of(
                element(Respondent.builder()
                    .party(RespondentParty.builder()
                        .lastName("Simpson")
                        .build())
                    .build()));

            NoticeOfChangeAnswers noticeOfChangeAnswers0 = NoticeOfChangeAnswers.builder()
                .applicantName("Swansea City Council")
                .respondentLastName("Simpson")
                .build();

            CaseDetails caseDetails = caseDetails(respondents, applicants, migrationId);
            caseDetails.getData().put("noticeOfChangeAnswers0", noticeOfChangeAnswers0);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format("Migration failed on case %s: Unexpected migration", caseId));
        }

        @Test
        void shouldThrowAnExceptionIfCaseContainsRespondentPolicyData() {
            List<Element<Applicant>> applicants = buildApplicants();

            List<Element<Respondent>> respondents = List.of(
                element(Respondent.builder()
                    .party(RespondentParty.builder()
                        .lastName("Simpson")
                        .build())
                    .build()));

            OrganisationPolicy respondentPolicy0 = OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(SolicitorRole.SOLICITORA.getCaseRoleLabel())
                .organisation(Organisation.builder()
                    .organisationName("Some org")
                    .organisationID("WA123")
                    .build())
                .build();

            CaseDetails caseDetails = caseDetails(respondents, applicants, migrationId);
            caseDetails.getData().put("respondentPolicy0", respondentPolicy0);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format("Migration failed on case %s: Unexpected migration", caseId));
        }

        @Test
        void shouldThrowAnExceptionIfCaseContainsMoreThanTenRespondents() {
            List<Element<Respondent>> respondents = List.of(
                element(Respondent.builder().build()),
                element(Respondent.builder().build()),
                element(Respondent.builder().build()),
                element(Respondent.builder().build()),
                element(Respondent.builder().build()),
                element(Respondent.builder().build()),
                element(Respondent.builder().build()),
                element(Respondent.builder().build()),
                element(Respondent.builder().build()),
                element(Respondent.builder().build()),
                element(Respondent.builder().build()));

            CaseDetails caseDetails = caseDetails(respondents, null, migrationId);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format("Migration failed on case %s: Case has %s respondents", caseId,
                    respondents.size()));
        }

        private CaseDetails caseDetails(List<Element<Respondent>> respondents,
                                        List<Element<Applicant>> applicants,
                                        String migrationId) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .state(State.SUBMITTED)
                .respondents1(respondents)
                .applicants(applicants)
                .id(caseId)
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }

        private List<Element<Applicant>> buildApplicants() {
            return List.of(element(Applicant.builder()
                .party(ApplicantParty.builder()
                    .organisationName("Swansea City Council")
                    .build())
                .build()));
        }

        private OrganisationPolicy buildOrganisationPolicy(SolicitorRole solicitorRole) {
            return OrganisationPolicy.builder()
                .organisation(Organisation.builder().build())
                .orgPolicyCaseAssignedRole(solicitorRole.getCaseRoleLabel())
                .build();
        }
    }
}
