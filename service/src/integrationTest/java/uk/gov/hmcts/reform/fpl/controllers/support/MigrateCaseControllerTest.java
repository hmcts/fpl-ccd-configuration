package uk.gov.hmcts.reform.fpl.controllers.support;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.enums.Event;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.CourtAdminDocument;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Solicitor;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.submission.EventValidationErrors;
import uk.gov.hmcts.reform.fpl.model.tasklist.Task;
import uk.gov.hmcts.reform.fpl.service.TaskListRenderer;
import uk.gov.hmcts.reform.fpl.service.TaskListService;
import uk.gov.hmcts.reform.fpl.service.validators.CaseSubmissionChecker;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.model.OrganisationPolicy.organisationPolicy;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.ColleagueRole.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.ColleagueRole.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractCallbackTest {
    public static final DocumentReference DOCUMENT_REFERENCE = testDocumentReference();


    MigrateCaseControllerTest() {
        super("migrate-case");
    }

    private static final String INVALID_MIGRATION_ID = "invalid id";

    @MockBean
    private TaskListService taskListService;

    @MockBean
    private TaskListRenderer taskListRenderer;

    @MockBean
    private CaseSubmissionChecker caseSubmissionChecker;

    @Test
    void shouldThrowExceptionWhenMigrationNotMappedForMigrationID() {
        CaseData caseData = CaseData.builder().build();

        assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, INVALID_MIGRATION_ID)))
            .getRootCause()
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("No migration mapped to " + INVALID_MIGRATION_ID);
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Dfpl164 {
        private final String migrationId = "DFPL-164";
        private final long validCaseId = 1626258358022834L;
        private final long invalidCaseId = 1626258358022000L;

        @Test
        void shouldPerformMigration() {
            CaseData caseData = CaseData.builder()
                .id(validCaseId)
                .state(State.SUBMITTED)
                .otherCourtAdminDocuments(
                    wrapElements(
                        CourtAdminDocument.builder()
                            .document(DOCUMENT_REFERENCE)
                            .documentTitle("court-document1")
                            .build(),
                        CourtAdminDocument.builder()
                            .document(
                                DocumentReference.builder()
                                    .filename("LA Certificate.pdf")
                                    .build()
                            )
                            .documentTitle("court-document1").build())
                )
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId)
            );
            assertThat(response.getData().get("documentViewHMCTS"))
                .asInstanceOf(InstanceOfAssertFactories.STRING)
                .doesNotContain("LA Certificate.pdf");

            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getOtherCourtAdminDocuments()).hasSize(1)
                .first()
                .extracting(doc -> doc.getValue().getDocumentTitle())
                .isEqualTo("court-document1");
        }

        @Test
        void shouldThrowAssersionError() {
            CaseData caseData = CaseData.builder()
                .id(invalidCaseId)
                .state(State.SUBMITTED)
                .otherCourtAdminDocuments(
                    wrapElements(
                        CourtAdminDocument.builder()
                            .document(DOCUMENT_REFERENCE)
                            .documentTitle("court-document1")
                            .build(),
                        CourtAdminDocument.builder()
                            .document(
                                DocumentReference.builder()
                                    .filename("LA Certificate.pdf")
                                    .build()
                            )
                            .documentTitle("court-document1").build())
                )
                .build();
            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage("Migration {id = DFPL-164, case reference = 1626258358022000},"
                    + " expected case id 1626258358022834");
        }

        @Test
        void shouldThrowErrorWhenCertificateNotFound() {
            CaseData caseData = CaseData.builder()
                .id(validCaseId)
                .state(State.SUBMITTED)
                .otherCourtAdminDocuments(
                    wrapElements(
                        CourtAdminDocument.builder()
                            .document(DOCUMENT_REFERENCE)
                            .documentTitle("court-document1")
                            .build(),
                        CourtAdminDocument.builder()
                            .document(DOCUMENT_REFERENCE)
                            .documentTitle("court-document2")
                            .build()
                    )
                )
                .build();
            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(NoSuchElementException.class);
        }
    }

    private CaseDetails buildCaseDetails(CaseData caseData, String migrationId) {
        CaseDetails caseDetails = asCaseDetails(caseData);
        caseDetails.getData().put("migrationId", migrationId);
        return caseDetails;
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Fpla3238 {

        private final String migrationId = "FPLA-3238";

        final OrganisationPolicy designatedOrg = organisationPolicy(
            "ORG1",
            "Name",
            LASOLICITOR);

        final ApplicantParty legacyApplicant = ApplicantParty.builder()
            .organisationName("Applicant org")
            .email(EmailAddress.builder()
                .email("applicant@legacy.com")
                .build())
            .telephoneNumber(Telephone.builder()
                .telephoneNumber("0777777777")
                .build())
            .mobileNumber(Telephone.builder()
                .telephoneNumber("0888888888")
                .build())
            .address(Address.builder()
                .addressLine1("Applicant office")
                .postcode("AP 999")
                .build())
            .pbaNumber("PBA7654321")
            .customerReference("APPLICANT_REF")
            .clientCode("APPLICANT_CODE")
            .build();

        final Solicitor legacySolicitor = Solicitor.builder()
            .name("Applicant solicitor")
            .mobile("0111111111")
            .telephone("0222222222")
            .dx("SOLICITOR_DX")
            .reference("SOLICITOR_REFERENCE")
            .email("solicitor@legacy.com")
            .build();

        final String expectedTaskList = "<h1>Task list</h1>";

        @BeforeEach
        void init() {

            final List<Task> tasks = List.of(Task.builder()
                .event(Event.SELECT_COURT)
                .build());

            final List<EventValidationErrors> tasksErrors = List.of(EventValidationErrors.builder()
                .errors(List.of("Error1", "Error2"))
                .event(Event.SELECT_COURT)
                .build());

            when(taskListService.getTasksForOpenCase(any())).thenReturn(tasks);
            when(caseSubmissionChecker.validateAsGroups(any())).thenReturn(tasksErrors);
            when(taskListRenderer.render(tasks, tasksErrors)).thenReturn(expectedTaskList);
        }

        @Test
        void shouldMigrateTaskListWhenCaseInOpenState() {

            final CaseData caseData = CaseData.builder()
                .state(State.OPEN)
                .applicants(wrapElements(Applicant.builder().party(legacyApplicant).build()))
                .solicitor(legacySolicitor)
                .localAuthorityPolicy(designatedOrg)
                .build();

            Map<String, Object> data = postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)).getData();

            assertThat(data.get("taskList")).isEqualTo(expectedTaskList);
        }

        @ParameterizedTest
        @EnumSource(value = State.class, names = {"OPEN"}, mode = EnumSource.Mode.EXCLUDE)
        void shouldNotMigrateTaskListWhenCaseNotInOpenState(State caseState) {

            final CaseData caseData = CaseData.builder()
                .state(caseState)
                .applicants(wrapElements(Applicant.builder().party(legacyApplicant).build()))
                .solicitor(legacySolicitor)
                .localAuthorityPolicy(designatedOrg)
                .build();

            Map<String, Object> data = postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)).getData();

            assertThat(data.get("taskList")).isNull();
        }

        @Test
        void shouldMigrateLegacyApplicantAndSolicitor() {

            final CaseData caseData = CaseData.builder()
                .applicants(wrapElements(Applicant.builder().party(legacyApplicant).build()))
                .solicitor(legacySolicitor)
                .localAuthorityPolicy(designatedOrg)
                .build();

            final LocalAuthority expectedLocalAuthority = LocalAuthority.builder()
                .id(designatedOrg.getOrganisation().getOrganisationID())
                .name(legacyApplicant.getOrganisationName())
                .designated("Yes")
                .address(legacyApplicant.getAddress())
                .email(legacyApplicant.getEmail().getEmail())
                .phone(legacyApplicant.getTelephoneNumber().getTelephoneNumber())
                .pbaNumber(legacyApplicant.getPbaNumber())
                .customerReference(legacyApplicant.getCustomerReference())
                .clientCode(legacyApplicant.getClientCode())
                .colleagues(ElementUtils.wrapElements(
                    Colleague.builder()
                        .role(OTHER)
                        .email(legacyApplicant.getEmail().getEmail())
                        .title(legacyApplicant.getJobTitle())
                        .phone("0777777777")
                        .mainContact("Yes")
                        .notificationRecipient("Yes")
                        .build(),
                    Colleague.builder()
                        .role(SOLICITOR)
                        .email(legacySolicitor.getEmail())
                        .dx(legacySolicitor.getDx())
                        .reference(legacySolicitor.getReference())
                        .fullName(legacySolicitor.getName())
                        .phone(legacySolicitor.getTelephone())
                        .notificationRecipient("Yes")
                        .mainContact("No")
                        .build()))
                .build();

            CaseData responseData = extractCaseData(postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)));

            assertThat(responseData.getLocalAuthorities())
                .extracting(Element::getValue)
                .containsExactly(expectedLocalAuthority);
        }

        @Test
        void shouldUseMobileNumberWhenNoTelephoneNumber() {

            final ApplicantParty applicant = legacyApplicant.toBuilder()
                .telephoneNumber(Telephone.builder()
                    .telephoneNumber(null)
                    .build())
                .build();

            final Solicitor solicitor = legacySolicitor.toBuilder()
                .telephone("")
                .build();

            final CaseData caseData = CaseData.builder()
                .applicants(wrapElements(Applicant.builder().party(applicant).build()))
                .solicitor(solicitor)
                .localAuthorityPolicy(designatedOrg)
                .build();

            final LocalAuthority expectedLocalAuthority = LocalAuthority.builder()
                .id(designatedOrg.getOrganisation().getOrganisationID())
                .name(applicant.getOrganisationName())
                .designated("Yes")
                .address(applicant.getAddress())
                .email(applicant.getEmail().getEmail())
                .phone(applicant.getMobileNumber().getTelephoneNumber())
                .pbaNumber(applicant.getPbaNumber())
                .customerReference(applicant.getCustomerReference())
                .clientCode(applicant.getClientCode())
                .colleagues(ElementUtils.wrapElements(
                    Colleague.builder()
                        .role(OTHER)
                        .email(applicant.getEmail().getEmail())
                        .title(applicant.getJobTitle())
                        .phone(applicant.getMobileNumber().getTelephoneNumber())
                        .mainContact("Yes")
                        .notificationRecipient("Yes")
                        .build(),
                    Colleague.builder()
                        .role(SOLICITOR)
                        .email(solicitor.getEmail())
                        .dx(solicitor.getDx())
                        .reference(solicitor.getReference())
                        .fullName(solicitor.getName())
                        .phone(solicitor.getMobile())
                        .notificationRecipient("Yes")
                        .mainContact("No")
                        .build()))
                .build();

            CaseData responseData = extractCaseData(postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)));

            assertThat(responseData.getLocalAuthorities())
                .extracting(Element::getValue)
                .containsExactly(expectedLocalAuthority);
        }

        @Test
        void shouldMarkSolicitorAsMainContactWhenNoDataForOtherColleague() {

            final ApplicantParty applicant = legacyApplicant.toBuilder()
                .email(null)
                .jobTitle(null)
                .mobileNumber(null)
                .telephoneNumber(null)
                .build();

            final CaseData caseData = CaseData.builder()
                .applicants(wrapElements(Applicant.builder().party(applicant).build()))
                .solicitor(legacySolicitor)
                .localAuthorityPolicy(designatedOrg)
                .build();

            final LocalAuthority expectedLocalAuthority = LocalAuthority.builder()
                .id(designatedOrg.getOrganisation().getOrganisationID())
                .name(applicant.getOrganisationName())
                .designated("Yes")
                .address(applicant.getAddress())
                .pbaNumber(applicant.getPbaNumber())
                .customerReference(applicant.getCustomerReference())
                .clientCode(applicant.getClientCode())
                .colleagues(ElementUtils.wrapElements(
                    Colleague.builder()
                        .role(SOLICITOR)
                        .email(legacySolicitor.getEmail())
                        .dx(legacySolicitor.getDx())
                        .reference(legacySolicitor.getReference())
                        .fullName(legacySolicitor.getName())
                        .phone(legacySolicitor.getTelephone())
                        .notificationRecipient("Yes")
                        .mainContact("Yes")
                        .build()))
                .build();

            CaseData responseData = extractCaseData(postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)));

            assertThat(responseData.getLocalAuthorities())
                .extracting(Element::getValue)
                .containsExactly(expectedLocalAuthority);
        }

        @Test
        void shouldMigrateLegacyApplicantWithoutSolicitor() {

            final CaseData caseData = CaseData.builder()
                .applicants(wrapElements(Applicant.builder().party(legacyApplicant).build()))
                .localAuthorityPolicy(designatedOrg)
                .build();

            final LocalAuthority expectedLocalAuthority = LocalAuthority.builder()
                .id(designatedOrg.getOrganisation().getOrganisationID())
                .name(legacyApplicant.getOrganisationName())
                .designated("Yes")
                .address(legacyApplicant.getAddress())
                .email(legacyApplicant.getEmail().getEmail())
                .phone(legacyApplicant.getTelephoneNumber().getTelephoneNumber())
                .pbaNumber(legacyApplicant.getPbaNumber())
                .customerReference(legacyApplicant.getCustomerReference())
                .clientCode(legacyApplicant.getClientCode())
                .colleagues(ElementUtils.wrapElements(
                    Colleague.builder()
                        .role(OTHER)
                        .email(legacyApplicant.getEmail().getEmail())
                        .title(legacyApplicant.getJobTitle())
                        .phone("0777777777")
                        .mainContact("Yes")
                        .notificationRecipient("Yes")
                        .build()))
                .build();

            CaseData responseData = extractCaseData(postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)));

            assertThat(responseData.getLocalAuthorities())
                .extracting(Element::getValue)
                .containsExactly(expectedLocalAuthority);
        }

        @Test
        void shouldMigrateLegacyApplicantWithoutColleagues() {

            final ApplicantParty applicant = legacyApplicant.toBuilder()
                .email(null)
                .jobTitle(null)
                .mobileNumber(null)
                .telephoneNumber(null)
                .build();

            final CaseData caseData = CaseData.builder()
                .applicants(wrapElements(Applicant.builder().party(applicant).build()))
                .localAuthorityPolicy(designatedOrg)
                .build();

            final LocalAuthority expectedLocalAuthority = LocalAuthority.builder()
                .id(designatedOrg.getOrganisation().getOrganisationID())
                .name(applicant.getOrganisationName())
                .designated("Yes")
                .address(applicant.getAddress())
                .pbaNumber(applicant.getPbaNumber())
                .customerReference(applicant.getCustomerReference())
                .clientCode(applicant.getClientCode())
                .colleagues(emptyList())
                .build();

            CaseData responseData = extractCaseData(postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)));

            assertThat(responseData.getLocalAuthorities())
                .extracting(Element::getValue)
                .containsExactly(expectedLocalAuthority);
        }

        @Test
        void shouldNotMigrateWhenLocalAuthorityExists() {

            final LocalAuthority initialLocalAuthority = LocalAuthority.builder()
                .id(designatedOrg.getOrganisation().getOrganisationID())
                .name("Initial")
                .build();

            final CaseData caseData = CaseData.builder()
                .applicants(wrapElements(Applicant.builder().party(legacyApplicant).build()))
                .solicitor(legacySolicitor)
                .localAuthorities(wrapElements(initialLocalAuthority))
                .localAuthorityPolicy(designatedOrg)
                .build();

            CaseData responseData = extractCaseData(postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)));

            assertThat(responseData.getLocalAuthorities())
                .extracting(Element::getValue)
                .containsExactly(initialLocalAuthority);

        }

        @Test
        void shouldNotMigrateWhenNoLegacyApplicant() {

            final CaseData caseData = CaseData.builder()
                .localAuthorityPolicy(designatedOrg)
                .build();

            CaseData responseData = extractCaseData(postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)));

            assertThat(responseData.getLocalAuthorities()).isEmpty();
        }

    }
}
