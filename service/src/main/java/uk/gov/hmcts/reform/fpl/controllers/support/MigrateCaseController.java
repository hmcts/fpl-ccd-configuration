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
import uk.gov.hmcts.reform.fpl.service.MigrateCFVService;
import uk.gov.hmcts.reform.fpl.service.MigrateCaseService;
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrderDocumentScopedFieldsCalculator;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Consumer;

import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY;

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
        "DFPL-log", this::runLogMigration,
        "DFPL-1947", this::run1947,
        "DFPL-1959", this::run1959,
        "DFPL-1991", this::run1991
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

    private void runLogMigration(CaseDetails caseDetails) {
        log.info("Dummy migration for case {}", caseDetails.getId());
    }

    private void run1947(CaseDetails caseDetails) {
        var migrationId = "DFPL-1947";
        var possibleCaseIds = List.of(1676634658659567L);
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);

        UUID documentId = UUID.fromString("492e4156-3066-4b1f-9b07-26347c21ae51");

        CaseData caseData = getCaseData(caseDetails);

        migrateCaseService.verifyStandardDirectionOrderExists(caseData, migrationId, documentId);
        caseDetails.getData().remove("standardDirectionOrder");
    }

    private void run1959(CaseDetails caseDetails) {
        var migrationId = "DFPL-1959";
        var possibleCaseIds = List.of(1701964866232462L);
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);

        UUID documentId = UUID.fromString("a4218369-872d-4270-a703-55e20416b4eb");

        CaseData caseData = getCaseData(caseDetails);

        migrateCaseService.verifyUrgentDirectionsOrderExists(caseData, migrationId, documentId);
        caseDetails.getData().remove("urgentDirectionsOrder");
    }

    public void run1991(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        caseDetails.getData().putAll(migrateCFVService.migrateMissingApplicationDocuments(caseData,
            DESIGNATED_LOCAL_AUTHORITY, List.of(LASOLICITOR)));
    }
}
