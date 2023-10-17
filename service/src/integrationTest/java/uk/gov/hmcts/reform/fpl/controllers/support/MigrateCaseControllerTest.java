package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.rd.JudicialUsersConfiguration;
import uk.gov.hmcts.reform.fpl.config.rd.LegalAdviserUsersConfiguration;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.JudicialUser;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.JudicialService;
import uk.gov.hmcts.reform.fpl.service.MigrateCFVService;
import uk.gov.hmcts.reform.fpl.service.TaskListRenderer;
import uk.gov.hmcts.reform.fpl.service.TaskListService;
import uk.gov.hmcts.reform.fpl.service.validators.CaseSubmissionChecker;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractCallbackTest {

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

    @MockBean
    private MigrateCFVService migrateCFVService;

    @MockBean
    private JudicialUsersConfiguration judicialUsersConfiguration;

    @MockBean
    private JudicialService judicialService;

    @MockBean
    private LegalAdviserUsersConfiguration legalAdviserUsersConfiguration;

    @Test
    void shouldThrowExceptionWhenMigrationNotMappedForMigrationID() {
        CaseData caseData = CaseData.builder().build();

        assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, INVALID_MIGRATION_ID)))
            .getRootCause()
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("No migration mapped to " + INVALID_MIGRATION_ID);
    }

    private CaseDetails buildCaseDetails(CaseData caseData, String migrationId) {
        CaseDetails caseDetails = asCaseDetails(caseData);
        caseDetails.getData().put("migrationId", migrationId);
        return caseDetails;
    }

    @BeforeEach
    void setup() {
        givenSystemUser();
        givenFplService();
    }

    @Nested
    class DfplAm {

        private final String migrationId = "DFPL-AM";

        @BeforeEach
        void beforeEach() {
            when(judicialService.getHearingJudgeRolesForMigration(any())).thenReturn(List.of());
        }

        @Test
        void shouldUpdateAllocatedJudgeId() {
            when(judicialUsersConfiguration.getJudgeUUID("test@test.com")).thenReturn(Optional.of("12345"));
            when(judicialService.getJudgeUserIdFromEmail("test@test.com")).thenReturn(Optional.of("12345"));
            when(legalAdviserUsersConfiguration.getLegalAdviserUUID("test@test.com")).thenReturn(Optional.empty());
            CaseData caseData = CaseData.builder()
                .id(12345L)
                .allocatedJudge(Judge.builder()
                    .judgeTitle(JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE)
                    .judgeLastName("Test")
                    .judgeEmailAddress("test@test.com")
                    .build())
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId));
            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getAllocatedJudge()).extracting("judgeJudicialUser")
                .isEqualTo(JudicialUser.builder()
                    .idamId("12345")
                    .build());
        }

        @Test
        void shouldUpdateAllocatedJudgeIdIfLegalAdviser() {
            when(judicialService.getJudgeUserIdFromEmail("test@test.com")).thenReturn(Optional.of("12345"));
            when(legalAdviserUsersConfiguration.getLegalAdviserUUID("test@test.com")).thenReturn(Optional.of("12345"));
            when(judicialUsersConfiguration.getJudgeUUID("test@test.com")).thenReturn(Optional.empty());
            CaseData caseData = CaseData.builder()
                .id(12345L)
                .allocatedJudge(Judge.builder()
                    .judgeTitle(JudgeOrMagistrateTitle.LEGAL_ADVISOR)
                    .judgeLastName("Test")
                    .judgeEmailAddress("test@test.com")
                    .build())
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId));
            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getAllocatedJudge()).extracting("judgeJudicialUser")
                .isEqualTo(JudicialUser.builder()
                    .idamId("12345")
                    .build());
        }


        @Test
        void shouldUpdateHearingJudgeIdIfLegalAdviser() {
            when(judicialService.getJudgeUserIdFromEmail("test@test.com")).thenReturn(Optional.of("12345"));
            when(legalAdviserUsersConfiguration.getLegalAdviserUUID("test@test.com")).thenReturn(Optional.of("12345"));
            when(judicialUsersConfiguration.getJudgeUUID("test@test.com")).thenReturn(Optional.empty());
            CaseData caseData = CaseData.builder()
                .id(12345L)
                .hearingDetails(ElementUtils.wrapElements(
                    HearingBooking.builder()
                        .startDate(now().plusDays(5))
                        .endDate(now().plusDays(6))
                        .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                            .judgeTitle(JudgeOrMagistrateTitle.LEGAL_ADVISOR)
                            .judgeLastName("Test")
                            .judgeEmailAddress("test@test.com")
                            .build())
                        .build()
                ))
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId));
            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getHearingDetails()).hasSize(1);
            assertThat(responseData.getHearingDetails().get(0).getValue().getJudgeAndLegalAdvisor())
                .extracting("judgeJudicialUser")
                .isEqualTo(JudicialUser.builder()
                    .idamId("12345")
                    .build());
        }

        @Test
        void shouldUpdateHearingJudgeIdIfJudge() {
            when(judicialService.getJudgeUserIdFromEmail("test@test.com")).thenReturn(Optional.of("12345"));
            when(judicialUsersConfiguration.getJudgeUUID("test@test.com")).thenReturn(Optional.of("12345"));
            when(legalAdviserUsersConfiguration.getLegalAdviserUUID("test@test.com")).thenReturn(Optional.empty());
            CaseData caseData = CaseData.builder()
                .id(12345L)
                .hearingDetails(ElementUtils.wrapElements(
                    HearingBooking.builder()
                        .startDate(now().plusDays(5))
                        .endDate(now().plusDays(6))
                        .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                            .judgeTitle(JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE)
                            .judgeLastName("Test")
                            .judgeEmailAddress("test@test.com")
                            .build())
                        .build()
                ))
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId));
            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getHearingDetails()).hasSize(1);
            assertThat(responseData.getHearingDetails().get(0).getValue().getJudgeAndLegalAdvisor())
                .extracting("judgeJudicialUser")
                .isEqualTo(JudicialUser.builder()
                    .idamId("12345")
                    .build());
        }

        @Test
        void shouldLeaveHearingsUnchangedIfNotInMapping() {
            when(judicialService.getJudgeUserIdFromEmail("test@test.com")).thenReturn(Optional.empty());
            when(judicialUsersConfiguration.getJudgeUUID("test@test.com")).thenReturn(Optional.empty());
            when(legalAdviserUsersConfiguration.getLegalAdviserUUID("test@test.com")).thenReturn(Optional.empty());
            CaseData caseData = CaseData.builder()
                .id(12345L)
                .hearingDetails(ElementUtils.wrapElements(
                    HearingBooking.builder()
                        .startDate(now().plusDays(5))
                        .endDate(now().plusDays(6))
                        .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                            .judgeTitle(JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE)
                            .judgeLastName("Test")
                            .judgeEmailAddress("test@test.com")
                            .build())
                        .build()
                ))
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId));
            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getHearingDetails()).isEqualTo(caseData.getHearingDetails());
        }

        @Test
        void shouldHaveNoChangeToHearingsIfNone() {
            CaseData caseData = CaseData.builder()
                .id(12345L)
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId));
            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getHearingDetails()).isNull();
        }

        @Test
        void shouldDoRollback() {
            CaseData caseData = CaseData.builder()
                .id(12345L)
                .allocatedJudge(Judge.builder()
                    .judgeTitle(JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE)
                    .judgeLastName("Test")
                    .judgeEmailAddress("test@test.com")
                    .judgeJudicialUser(JudicialUser.builder()
                        .idamId("12345")
                        .build())
                    .build())
                .hearingDetails(ElementUtils.wrapElements(
                    HearingBooking.builder()
                        .startDate(now().plusDays(5))
                        .endDate(now().plusDays(6))
                        .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                            .judgeTitle(JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE)
                            .judgeLastName("Test")
                            .judgeEmailAddress("test@test.com")
                            .judgeJudicialUser(JudicialUser.builder()
                                .idamId("12345")
                                .build())
                            .build())
                        .build()
                ))
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId + "-Rollback"));
            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getHearingDetails()).hasSize(1);
            assertThat(responseData.getHearingDetails().get(0).getValue().getJudgeAndLegalAdvisor())
                .extracting("judgeJudicialUser")
                .isNull();
            assertThat(responseData.getAllocatedJudge()).extracting("judgeJudicialUser").isNull();
        }

        @Test
        void shouldHaveNoChangeToHearingsInRollbackIfNone() {
            CaseData caseData = CaseData.builder()
                .id(12345L)
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId + "-Rollback"));
            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getHearingDetails()).isNull();
        }

    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class DfplCFV {
        final String migrationId = "DFPL-CFV";

        @Test
        void shouldInvokeAllMigrations() {
            final CaseData caseData = CaseData.builder()
                .id(nextLong())
                .build();

            CaseDetails caseDetails = buildCaseDetails(caseData, migrationId);
            doNothing()
                .when(migrateCFVService).doHasCFVMigratedCheck(anyLong(), any(), eq(migrationId), eq(true));
            postAboutToSubmitEvent(caseDetails);

            verify(migrateCFVService).migratePositionStatementChild(any());
            verify(migrateCFVService).migratePositionStatementRespondent(any());
            verify(migrateCFVService).migrateNoticeOfActingOrIssue(any());
            verify(migrateCFVService).migrateGuardianReports(any());
            verify(migrateCFVService).migrateExpertReports(any());
            verify(migrateCFVService).migrateApplicantWitnessStatements(any());
            verify(migrateCFVService).migrateRespondentStatement(any());
            verify(migrateCFVService).migrateSkeletonArgumentList(any());
            verify(migrateCFVService).migrateApplicationDocuments(any());
            verify(migrateCFVService).migrateCorrespondenceDocuments(any());
            verify(migrateCFVService).moveCaseSummaryWithConfidentialAddressToCaseSummaryListLA(any());
            verify(migrateCFVService).migrateCourtBundle(any());
        }

        @Test
        void shouldNotInvokeAnyWhenDataWasMigrated() {
            final CaseData caseData = CaseData.builder()
                .id(nextLong())
                .build();

            doThrow(AssertionError.class).when(migrateCFVService).doHasCFVMigratedCheck(anyLong(), any(),
                eq(migrationId));

            CaseDetails caseDetails = buildCaseDetails(caseData, migrationId);
            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .isInstanceOf(AssertionError.class);

            verify(migrateCFVService, times(0)).migratePositionStatementChild(any());
            verify(migrateCFVService, times(0)).migratePositionStatementRespondent(any());
            verify(migrateCFVService, times(0)).migrateNoticeOfActingOrIssue(any());
            verify(migrateCFVService, times(0)).migrateGuardianReports(any());
            verify(migrateCFVService, times(0)).migrateExpertReports(any());
            verify(migrateCFVService, times(0)).migrateApplicantWitnessStatements(any());
            verify(migrateCFVService, times(0)).migrateRespondentStatement(any());
            verify(migrateCFVService, times(0)).migrateSkeletonArgumentList(any());
            verify(migrateCFVService, times(0)).migrateApplicationDocuments(any());
            verify(migrateCFVService, times(0)).migrateCorrespondenceDocuments(any());
            verify(migrateCFVService, times(0)).moveCaseSummaryWithConfidentialAddressToCaseSummaryListLA(any());
            verify(migrateCFVService, times(0)).migrateCourtBundle(any());
        }
    }

    @Nested
    class DfplCFVRollback {
        final String migrationId = "DFPL-CFV-Rollback";

        @Test
        void shouldInvokeAllRollbackScripts() {
            final CaseData caseData = CaseData.builder()
                .id(nextLong())
                .build();

            CaseDetails caseDetails = buildCaseDetails(caseData, migrationId);
            caseDetails.getData().put("hasBeenCFVMigrated", "Yes");

            doNothing()
                .when(migrateCFVService).doHasCFVMigratedCheck(anyLong(), any(), eq(migrationId), eq(true));
            postAboutToSubmitEvent(caseDetails);

            verify(migrateCFVService).rollbackPositionStatementChild(any());
            verify(migrateCFVService).rollbackPositionStatementRespondent(any());
            verify(migrateCFVService).rollbackNoticeOfActingOrIssue();
            verify(migrateCFVService).rollbackGuardianReports();
            verify(migrateCFVService).rollbackExpertReports();
            verify(migrateCFVService).rollbackApplicantWitnessStatements();
            verify(migrateCFVService).rollbackRespondentStatement();
            verify(migrateCFVService).rollbackSkeletonArgumentList(any());
            verify(migrateCFVService).rollbackApplicationDocuments();
            verify(migrateCFVService).rollbackCorrespondenceDocuments();
            verify(migrateCFVService).rollbackCaseSummaryMigration(any());
            verify(migrateCFVService).rollbackCourtBundleMigration(any());
        }

        @Test
        void shouldNotInvokeAnyWhenDataWasNotMigrated() {
            final CaseData caseData = CaseData.builder()
                .id(nextLong())
                .build();

            CaseDetails caseDetails = buildCaseDetails(caseData, migrationId);

            doThrow(AssertionError.class).when(migrateCFVService)
                .doHasCFVMigratedCheck(anyLong(), any(), any(), eq(true));
            
            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .isInstanceOf(AssertionError.class);

            verify(migrateCFVService, times(0)).rollbackPositionStatementChild(any());
            verify(migrateCFVService, times(0)).rollbackPositionStatementRespondent(any());
            verify(migrateCFVService, times(0)).rollbackNoticeOfActingOrIssue();
            verify(migrateCFVService, times(0)).rollbackGuardianReports();
            verify(migrateCFVService, times(0)).rollbackExpertReports();
            verify(migrateCFVService, times(0)).rollbackApplicantWitnessStatements();
            verify(migrateCFVService, times(0)).rollbackRespondentStatement();
            verify(migrateCFVService, times(0)).rollbackSkeletonArgumentList(any());
            verify(migrateCFVService, times(0)).rollbackApplicationDocuments();
            verify(migrateCFVService, times(0)).rollbackCorrespondenceDocuments();
            verify(migrateCFVService, times(0)).rollbackCaseSummaryMigration(any());
            verify(migrateCFVService, times(0)).rollbackCourtBundleMigration(any());
        }
    }
}
