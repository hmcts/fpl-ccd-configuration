package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeAnswers;
import uk.gov.hmcts.reform.fpl.service.RespondentPolicyService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractCallbackTest {
    MigrateCaseControllerTest() {
        super("migrate-case");
    }

    @Autowired
    RespondentPolicyService respondentPolicyService;

    @Nested
    class Fpla2982 {
        String migrationId = "FPLA-2982";

        @ParameterizedTest
        @ValueSource(longs = {
            1598429153622508L,
            1615191831533551L,
            1594384486007055L,
            1601977974423857L,
            1615571327261140L,
            1615476016828466L,
            1616507805759840L,
            1610015759403189L,
            1615994076934396L,
            1611613172339094L,
            1612440806991994L,
            1607004182103389L,
            1617045146450299L,
            1612433400114865L,
            1615890702114702L,
            1610018233059619L})
        void shouldMigrateMissingC2IdCase(Long caseId) {
            CaseDetails caseDetails = caseDetails(migrationId,
                wrapElements(createAdditionalApplicationBundle(createC2DocumentBundle(null),
                    createOtherApplicationBundle(null))), caseId);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(!extractedCaseData.getAdditionalApplicationsBundle().isEmpty());

            extractedCaseData.getAdditionalApplicationsBundle()
                .forEach(bundle -> assertThat(bundle.getValue().getC2DocumentBundle().getId() != null));
        }

        @Test
        void shouldThrowExceptionForInvalidCaseId() {
            CaseDetails caseDetails = caseDetails(migrationId,
                wrapElements(createAdditionalApplicationBundle(createC2DocumentBundle(UUID.randomUUID()),
                    null)), 1234L);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("Invalid case Id");
        }

        @Test
        void shouldThrowExceptionIfNoNullIdsFound() {
            CaseDetails caseDetails = caseDetails(migrationId,
                wrapElements(
                    createAdditionalApplicationBundle(createC2DocumentBundle(UUID.randomUUID()),
                        createOtherApplicationBundle(UUID.randomUUID())),
                    createAdditionalApplicationBundle(createC2DocumentBundle(UUID.randomUUID()),
                        null),
                    createAdditionalApplicationBundle(null,
                        createOtherApplicationBundle(UUID.randomUUID()))
                ), 1601977974423857L);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("No c2DocumentBundle or otherApplicationsBundle found with missing Id");
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedMigrationId() {
            String incorrectMigrationId = "FPLA-9876";

            CaseDetails caseDetails = caseDetails(incorrectMigrationId,
                wrapElements(createAdditionalApplicationBundle(createC2DocumentBundle(null),
                    createOtherApplicationBundle(null))), 1615890702114702L);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(!extractedCaseData.getAdditionalApplicationsBundle().isEmpty());

            extractedCaseData.getAdditionalApplicationsBundle()
                .forEach(bundle -> assertThat(bundle.getValue().getC2DocumentBundle().getId() == null));
        }

        private C2DocumentBundle createC2DocumentBundle(UUID id) {
            return C2DocumentBundle.builder().id(id).build();
        }

        private OtherApplicationsBundle createOtherApplicationBundle(UUID id) {
            return OtherApplicationsBundle.builder().id(id).build();
        }

        private AdditionalApplicationsBundle createAdditionalApplicationBundle(
            C2DocumentBundle c2DocumentBundle,
            OtherApplicationsBundle otherApplicationsBundle) {
            return AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(c2DocumentBundle)
                .otherApplicationsBundle(otherApplicationsBundle)
                .build();
        }

        private CaseDetails caseDetails(String migrationId,
                                        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle,
                                        Long caseId) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .additionalApplicationsBundle(additionalApplicationsBundle)
                .id(caseId)
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Fpla2961 {
        Long caseId = 1111L;
        String migrationId = "FPLA-2961";

        private Stream<Arguments> invalidStates() {
            return Stream.of(
                Arguments.of(State.OPEN),
                Arguments.of(State.CLOSED),
                Arguments.of(State.DELETED)
            );
        }

        @Test
        void shouldGenerateRespondentPoliciesAndNoticeOfChangeAnswersFromFullyPopulatedCaseData() {
            List<Element<Applicant>> applicants = List.of(
                element(Applicant.builder()
                    .party(ApplicantParty.builder()
                        .organisationName("Swansea City Council")
                        .build())
                    .build()));

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
            List<Element<Applicant>> applicants = List.of();

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
            assertThat(extractedCaseData.getRespondentPolicyData()).isEqualTo(RespondentPolicyData.builder().build());
            assertThat(extractedCaseData.getNoticeOfChangeAnswersData()).isEqualTo(
                NoticeOfChangeAnswersData.builder().build());
        }

        @ParameterizedTest
        @MethodSource("invalidStates")
        void shouldNotMigrateCaseIfStateIsUnsupported(State caseState) {
            List<Element<Applicant>> applicants = List.of();

            List<Element<Respondent>> respondents = List.of(
                element(Respondent.builder()
                    .party(RespondentParty.builder()
                        .lastName("Simpson")
                        .build())
                    .build()));

            CaseDetails caseDetails = caseDetails(respondents, applicants, migrationId);
            caseDetails.setState(caseState.getValue());
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getRespondents1()).isEqualTo(respondents);
            assertThat(extractedCaseData.getApplicants()).isEqualTo(applicants);
            assertThat(extractedCaseData.getRespondentPolicyData()).isEqualTo(RespondentPolicyData.builder().build());
            assertThat(extractedCaseData.getNoticeOfChangeAnswersData()).isEqualTo(
                NoticeOfChangeAnswersData.builder().build());
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
                .hasMessage(String.format("Case %s has 11 respondents", caseId));
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

        private OrganisationPolicy buildOrganisationPolicy(SolicitorRole solicitorRole) {
            return OrganisationPolicy.builder()
                .organisation(Organisation.builder().build())
                .orgPolicyCaseAssignedRole(solicitorRole.getCaseRoleLabel())
                .build();
        }
    }
}
