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
        "DFPL-1957", this::run1957,
        "DFPL-1993", this::run1993,
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

    public void run1991(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        caseDetails.getData().putAll(migrateCFVService.migrateMissingApplicationDocuments(caseData,
            DESIGNATED_LOCAL_AUTHORITY, List.of(LASOLICITOR)));
    }
}
