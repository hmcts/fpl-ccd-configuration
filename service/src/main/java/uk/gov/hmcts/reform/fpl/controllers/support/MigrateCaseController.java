package uk.gov.hmcts.reform.fpl.controllers.support;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApiV2;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.CallbackController;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.JudicialService;
import uk.gov.hmcts.reform.fpl.service.MigrateCFVService;
import uk.gov.hmcts.reform.fpl.service.MigrateCaseService;

import java.util.Collection;
import java.util.LinkedHashMap;
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
    private final CoreCaseDataApiV2 coreCaseDataApi;
    private final RequestData requestData;
    private final AuthTokenGenerator authToken;

    private final MigrateCaseService migrateCaseService;
    private final MigrateCFVService migrateCFVService;
    private final JudicialService judicialService;

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-CFV", this::runCFV,
        "DFPL-CFV-Rollback", this::runCFVrollback,
        "DFPL-CFV-dry", this::dryRunCFV,
        "DFPL-1905", this::run1905
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
        migrateCFVService.validateMigratedNumberOfDocuments(migrationId, caseData, changes);
        pushChangesToCaseDetails(caseDetails, changes);
    }

    private void runCFVrollback(CaseDetails caseDetails) {
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

    private void run1905(CaseDetails caseDetails) {
        migrateCaseService.clearChangeOrganisationRequest(caseDetails);
    }
}
