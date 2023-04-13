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
import uk.gov.hmcts.reform.fpl.service.MigrateCaseService;
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrderDocumentScopedFieldsCalculator;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Consumer;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {
    private static final String MIGRATION_ID_KEY = "migrationId";

    private final MigrateCaseService migrateCaseService;
    private final ManageOrderDocumentScopedFieldsCalculator fieldsCalculator;

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-1320", this::run1320,
        "DFPL-1335", this::run1335,
        "DFPL-1261", this::run1261,
        "DFPL-1226", this::run1226,
        "DFPL-1361", this::run1361,
        "DFPL-1371", this::run1371
    );

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

    private void run1261(CaseDetails caseDetails) {
        var migrationId = "DFPL-1261";
        var possibleCaseIds = List.of(1661855469987973L);
        final UUID expectedOrderId = UUID.fromString("ef610598-8bfd-42c2-9edd-0cd142b45f07");
        final UUID expectedHearingOrderBundleId = UUID.fromString("2f588328-4f6c-4da6-817b-b8c007d2a61d");
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        caseDetails.getData().putAll(migrateCaseService.removeDraftUploadedCMOs(getCaseData(caseDetails),
            migrationId, expectedOrderId));
    }

    private void run1226(CaseDetails caseDetails) {
        migrateCaseService.doDocumentViewNCCheck(caseDetails.getId(), "DFPL-1226", caseDetails);
        caseDetails.getData().putAll(migrateCaseService.refreshDocumentViews(getCaseData(caseDetails)));
    }

    private void run1320(CaseDetails caseDetails) {
        String migrationId = "DFPL-1320";
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), List.of(1667466628958196L), migrationId);
        caseDetails.getData().putAll(
            migrateCaseService.removeJudicialMessage(getCaseData(caseDetails), migrationId,
                "afb1a77d-08c9-4ad1-a03f-e7b47c8eb8c3"));
    }

    private void run1335(CaseDetails caseDetails) {
        var migrationId = "DFPL-1335";
        var possibleCaseIds = List.of(1677078973744903L);
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        caseDetails.getData().putAll(migrateCaseService.removeSkeletonArgument(getCaseData(caseDetails),
            "e4e70bf5-4905-4c13-9d59-d20a202b6c9a", migrationId));
    }

    private void run1361(CaseDetails caseDetails) {
        var migrationId = "DFPL-1361";
        var possibleCaseIds = List.of(1680179801927341L, 1651064219316144L);
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        fieldsCalculator.calculate().forEach(caseDetails.getData()::remove);
    }

    private void run1371(CaseDetails caseDetails) {
        String migrationId = "DFPL-1371";
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), List.of(1667466628958196L), migrationId);
        caseDetails.getData().putAll(
            migrateCaseService.removeJudicialMessage(getCaseData(caseDetails), migrationId,
                "c6d4ed7b-ca76-47ea-87b7-9538762bab00"));
    }
}
