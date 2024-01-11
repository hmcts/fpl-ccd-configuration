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
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrderDocumentScopedFieldsCalculator;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Consumer;

@Api
@Slf4j
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MigrateCaseController extends CallbackController {
    public static final String MIGRATION_ID_KEY = "migrationId";
    private final ManageOrderDocumentScopedFieldsCalculator fieldsCalculator;
    private final MigrateCaseService migrateCaseService;
    private final MigrateCFVService migrateCFVService;

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-CFV", this::runCFV,
        "DFPL-CFV-Rollback", this::runCfvRollback,
        "DFPL-CFV-Failure", this::runCfvFailure,
        "DFPL-CFV-dry", this::dryRunCFV,
        "DFPL-1940", this::run1940,
        "DFPL-log", this::runLogMigration,
        "DFPL-1957", this::run1957,
        "DFPL-1993", this::run1993,
        "DFPL-1965", this::run1965
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

    private void run1940(CaseDetails caseDetails) {
        var migrationId = "DFPL-1940";
        var possibleCaseIds = List.of(1697791879605293L);
        var expectedMessageId = UUID.fromString("29b3eab8-1e62-4aa2-86d1-17874d27933e");

        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        CaseData caseData = getCaseData(caseDetails);
        caseDetails.getData().putAll(migrateCaseService.removeJudicialMessage(caseData, migrationId,
            String.valueOf(expectedMessageId)));
    }

    private void runLogMigration(CaseDetails caseDetails) {
        log.info("Dummy migration for case {}", caseDetails.getId());
    }

    private void run1957(CaseDetails caseDetails) {
        var migrationId = "DFPL-1957";
        var possibleCaseIds = List.of(1680274206281046L);
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        CaseData caseData = getCaseData(caseDetails);
        caseDetails.getData().putAll(migrateCaseService.removePositionStatementChild(caseData, migrationId, false,
            UUID.fromString("6a41564d-575c-4d88-a15a-d5fb5541a4d1"),
            UUID.fromString("f97c1f3f-5326-4ddb-bff4-e5438d0787f7"),
            UUID.fromString("9289e73e-91d9-4a0b-92f7-26d50b822be7"),
            UUID.fromString("4e506d22-e42e-456d-a7b5-398ad854ac7d")));
        caseDetails.getData().putAll(migrateCaseService.removePositionStatementRespondent(caseData, migrationId, false,
            UUID.fromString("d3f2f35a-e655-497a-8307-7560f968e702"),
            UUID.fromString("90bccc3a-fdff-40ba-9d44-65128e7ae402"),
            UUID.fromString("5bccccd3-5557-4544-8860-29719ebcd6f8"),
            UUID.fromString("78e65b7a-4703-4d85-9be9-9f73d71e9c71")));
    }

    private void run1993(CaseDetails caseDetails) {
        var migrationId = "DFPL-1993";
        var possibleCaseIds = List.of(1698315138943987L);
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        CaseData caseData = getCaseData(caseDetails);
        caseDetails.getData().putAll(migrateCaseService.removePositionStatementChild(caseData, migrationId, false,
            UUID.fromString("5572d526-7045-4fd6-86a6-136656dc4ef4")));
    }

    private void run1965(CaseDetails caseDetails) {
        var migrationId = "DFPL-1965";
        var possibleCaseId = 1690901925013890L;
        var manchesterOrgId = "JTLD1QJ";
        migrateCaseService.doCaseIdCheck(caseDetails.getId(), possibleCaseId, migrationId);
        caseDetails.getData().putAll(migrateCaseService.changeApplicantToLaManaging(manchesterOrgId));
    }
}
