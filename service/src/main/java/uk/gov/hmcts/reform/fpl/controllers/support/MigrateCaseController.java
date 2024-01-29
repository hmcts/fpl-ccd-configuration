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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.MigrateCaseService;
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrderDocumentScopedFieldsCalculator;

import java.util.Collection;
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

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-1940", this::run1940,
        "DFPL-1934", this::run1934,
        "DFPL-log", this::runLogMigration,
        "DFPL-2033", this::run2033,
        "DFPL-2094", this::run2094,
        "DFPL-2094-rollback", this::run2094Rollback
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

    private void run1934(CaseDetails caseDetails) {
        migrateCaseService.clearChangeOrganisationRequest(caseDetails);
    }

    private void runLogMigration(CaseDetails caseDetails) {
        log.info("Dummy migration for case {}", caseDetails.getId());
    }

    private void run2033(CaseDetails caseDetails) {
        var migrationId = "DFPL-2033";
        var possibleCaseIds = List.of(1700651082936197L);
        var expectedMessageId = UUID.fromString("d80b8dbe-889d-41f3-b9c5-ea3df08e53c7");

        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        CaseData caseData = getCaseData(caseDetails);
        caseDetails.getData().putAll(migrateCaseService.removeJudicialMessage(caseData, migrationId,
            String.valueOf(expectedMessageId)));
    }

    private void run2094(CaseDetails caseDetails) {
        var migrationId = "DFPL-2094";

        CaseData caseData = getCaseData(caseDetails);
        caseDetails.getData().putAll(migrateCaseService.migrateCaseClosedDateToLatestFinalOrderApprovalDate(caseData,
            migrationId));
    }

    private void run2094Rollback(CaseDetails caseDetails) {
        var migrationId = "DFPL-2094-rollback";

        CaseData caseData = getCaseData(caseDetails);
        caseDetails.getData().putAll(migrateCaseService.rollbackCloseCaseTabFieldMigration(caseData,
            migrationId));
    }
}
