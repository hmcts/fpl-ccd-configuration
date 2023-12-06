package uk.gov.hmcts.reform.fpl.controllers.support;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.CallbackController;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.MigrateCFVService;
import uk.gov.hmcts.reform.fpl.service.MigrateCaseService;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

@Api
@Slf4j
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MigrateCaseController extends CallbackController {
    public static final String MIGRATION_ID_KEY = "migrationId";

    private final MigrateCaseService migrateCaseService;
    private final MigrateCFVService migrateCFVService;

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-CFV", this::runCFV,
        "DFPL-CFV-Rollback", this::runCfvRollback,
        "DFPL-CFV-Failure", this::runCfvFailure,
        "DFPL-CFV-dry", this::dryRunCFV,
        "DFPL-1921", this::run1921,
        "DFPL-1940", this::run1940
    );

    private static void pushChangesToCaseDetails(CaseDetails caseDetails, Map<String, Object> changes) {
        for (Map.Entry<String, Object> entrySet : changes.entrySet()) {
            if (entrySet.getValue() == null || (entrySet.getValue() instanceof Collection
                && ((Collection) entrySet.getValue()).isEmpty())) {
                caseDetails.getData().remove(entrySet.getKey());
            } else {
                caseDetails.getData().put(entrySet.getKey(), entrySet.getValue());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void mergeChanges(Map<String, Object> target, Map<String, Object> newChanges) {
        newChanges.entrySet().forEach(entry -> {
            if (target.containsKey(entry.getKey())) {
                ((List) target.get(entry.getKey())).addAll((List) entry.getValue());
            } else {
                target.put(entry.getKey(), entry.getValue());
            }
        });
    }

    private Map<String, Object> prepareChangesForMigratingAllToArchivedDocuments(String migrationId,
                                                                                 CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        migrateCFVService.doHasCFVMigratedCheck(caseDetails.getId(), (String) caseDetails.getData()
            .get("hasBeenCFVMigrated"), migrationId);
        Map<String, Object> changes = new LinkedHashMap<>();
        mergeChanges(changes, migrateCFVService.migrateHearingFurtherEvidenceDocumentsToArchivedDocuments(caseData));
        mergeChanges(changes, migrateCFVService.migrateFurtherEvidenceDocumentsToArchivedDocuments(caseData));
        mergeChanges(changes, migrateCFVService.migrateCaseSummaryToArchivedDocuments(caseData));
        mergeChanges(changes, migrateCFVService.migratePositionStatementToArchivedDocuments(caseData));
        mergeChanges(changes, migrateCFVService.migrateRespondentStatementToArchivedDocuments(caseData));
        mergeChanges(changes, migrateCFVService.migrateCorrespondenceDocumentsToArchivedDocuments(caseData));
        mergeChanges(changes, migrateCFVService.migrateApplicationDocumentsToArchivedDocuments(caseData));
        mergeChanges(changes, migrateCFVService.migrateCourtBundlesToArchivedDocuments(caseData));
        changes.put("hasBeenCFVMigrated", YesNo.YES);
        return changes;
    }

    private Map<String, Object> prepareChangesForCFVMigration(String migrationId, CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        migrateCFVService.doHasCFVMigratedCheck(caseDetails.getId(), (String) caseDetails.getData()
            .get("hasBeenCFVMigrated"), migrationId);
        Map<String, Object> changes = new LinkedHashMap<>();
        changes.putAll(migrateCFVService.migrateApplicantWitnessStatements(caseData));
        changes.putAll(migrateCFVService.migrateApplicationDocuments(caseData));
        changes.putAll(migrateCFVService.migrateCourtBundle(caseData));
        changes.putAll(migrateCFVService.migrateCorrespondenceDocuments(caseData));
        changes.putAll(migrateCFVService.migrateExpertReports(caseData));
        changes.putAll(migrateCFVService.migrateGuardianReports(caseData));
        changes.putAll(migrateCFVService.migrateNoticeOfActingOrIssue(caseData));
        changes.putAll(migrateCFVService.migrateArchivedDocuments(caseData));
        changes.putAll(migrateCFVService.migratePositionStatementRespondent(caseData));
        changes.putAll(migrateCFVService.migratePositionStatementChild(caseData));
        changes.putAll(migrateCFVService.migrateRespondentStatement(caseData));
        changes.putAll(migrateCFVService.migrateSkeletonArgumentList(caseData));
        changes.putAll(migrateCFVService.moveCaseSummaryWithConfidentialAddressToCaseSummaryListLA(caseData));
        changes.put("hasBeenCFVMigrated", YesNo.YES);
        return changes;
    }

    private void dryRunCFV(CaseDetails caseDetails) {
        var migrationId = "DFPL-CFV-dry";
        CaseData caseData = getCaseData(caseDetails);
        Map<String, Object> changes = prepareChangesForCFVMigration(migrationId, caseDetails);
        migrateCFVService.validateMigratedNumberOfDocuments(migrationId, caseData, changes);
    }

    private void runCFV(CaseDetails caseDetails) {
        var migrationId = "DFPL-CFV";
        CaseData caseData = getCaseData(caseDetails);
        Map<String, Object> changes = prepareChangesForCFVMigration(migrationId, caseDetails);
        try {
            migrateCFVService.validateMigratedNumberOfDocuments(migrationId, caseData, changes);
        } catch (AssertionError ex) {
            changes = prepareChangesForMigratingAllToArchivedDocuments(migrationId, caseDetails);
        }
        pushChangesToCaseDetails(caseDetails, changes);
    }

    private void runCfvFailure(CaseDetails caseDetails) {
        var migrationId = "DFPL-CFV-Failure";
        CaseData caseData = getCaseData(caseDetails);
        Map<String, Object> changes = prepareChangesForMigratingAllToArchivedDocuments(migrationId,
            caseDetails);
        pushChangesToCaseDetails(caseDetails, changes);
    }

    private void runCfvRollback(CaseDetails caseDetails) {
        migrateCFVService.doHasCFVMigratedCheck(caseDetails.getId(), (String) caseDetails.getData()
                .get("hasBeenCFVMigrated"), "DFPL-CFV-Rollback", true);

        Map<String, Object> changes = new LinkedHashMap<>();
        changes.putAll(migrateCFVService.rollbackApplicantWitnessStatements());
        changes.putAll(migrateCFVService.rollbackApplicationDocuments());
        changes.putAll(migrateCFVService.rollbackCaseSummaryMigration(caseDetails));
        changes.putAll(migrateCFVService.rollbackCourtBundleMigration(caseDetails));
        changes.putAll(migrateCFVService.rollbackCorrespondenceDocuments());
        changes.putAll(migrateCFVService.rollbackExpertReports());
        changes.putAll(migrateCFVService.rollbackGuardianReports());
        changes.putAll(migrateCFVService.rollbackNoticeOfActingOrIssue());
        changes.putAll(migrateCFVService.rollbackRespondentStatement());
        changes.putAll(migrateCFVService.rollbackPositionStatementChild(caseDetails));
        changes.putAll(migrateCFVService.rollbackPositionStatementRespondent(caseDetails));
        changes.putAll(migrateCFVService.rollbackSkeletonArgumentList(caseDetails));
        changes.putAll(migrateCFVService.rollbackArchivedDocumentsList());
        changes.put("hasBeenCFVMigrated", null);
        pushChangesToCaseDetails(caseDetails, changes);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        String migrationId = (String) caseDetails.getData().get(MIGRATION_ID_KEY);
        Long id = caseDetails.getId();

        log.info("Migration {id = {}, case reference = {}} started", migrationId, id);

        if (!migrations.containsKey(migrationId)) {
            throw new NoSuchElementException("No migration mapped to " + migrationId);
        }

        migrations.get(migrationId).accept(caseDetails);

        log.info("Migration {id = {}, case reference = {}} finished", migrationId, id);

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run1921(CaseDetails caseDetails) {
        var migrationId = "DFPL-1921";
        var possibleCaseIds = List.of(1689599455058930L);

        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        CaseData caseData = getCaseData(caseDetails);
        caseDetails.getData().putAll(migrateCaseService.removeCaseSummaryByHearingId(caseData, migrationId,
            UUID.fromString("37ab2651-b3f6-40e2-b880-275a6dba51cd")));
    }

    private void run1940(CaseDetails caseDetails) {
        var migrationId = "DFPL-1940";
        var possibleCaseIds = List.of(1697791879605293L);
        var expectedMessageId = UUID.fromString("29b3eab8-1e62-4aa2-86d1-17874d27933e");

        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        CaseData caseData = getCaseData(caseDetails);
        caseDetails.getData().putAll(migrateCaseService.removeJudicialMessage(caseData, migrationId,
            String.valueOf(expectedMessageId)));
    }
}
