package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.MigrateCFVService;

import java.util.NoSuchElementException;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractCallbackTest {

    MigrateCaseControllerTest() {
        super("migrate-case");
    }

    private static final String INVALID_MIGRATION_ID = "invalid id";

    @MockBean
    private MigrateCFVService migrateCFVService;

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
