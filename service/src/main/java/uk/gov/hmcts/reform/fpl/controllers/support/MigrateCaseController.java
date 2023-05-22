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
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.service.MigrateCaseService;
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrderDocumentScopedFieldsCalculator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static java.lang.String.format;

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
        "DFPL-1261", this::run1261,
        "DFPL-1226", this::run1226,
        "DFPL-1361", this::run1361,
        "DFPL-1291", this::run1291,
        "DFPL-1310", this::run1310,
        "DFPL-1371", this::run1371,
        "DFPL-1380", this::run1380,
        "DFPL-1437", this::run1437,
        "DFPL-1242", this::run1242,
        "DFPL-1451", this::run1451
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

    private void run1361(CaseDetails caseDetails) {
        var migrationId = "DFPL-1361";
        var possibleCaseIds = List.of(1680179801927341L, 1651064219316144L);
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        fieldsCalculator.calculate().forEach(caseDetails.getData()::remove);
    }

    private void run1291(CaseDetails caseDetails) {
        var migrationId = "DFPL-1291";
        var possibleCaseIds = List.of(1620403322799028L, 1627403399420113L);
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        caseDetails.getData().putAll(migrateCaseService.addCourt("165")); // Carlisle
    }

    @SuppressWarnings("unchecked")
    private void run1310(CaseDetails caseDetails) {
        var migrationId = "DFPL-1310";

        Optional<Map> orders = Optional.ofNullable((Map) caseDetails.getData().get("orders"));
        if (orders.isPresent() && orders.get().get("court").equals("150")) {
            Map ordersMap = new HashMap<>(orders.get());
            ordersMap.put("court", "554");
            caseDetails.getData().put("orders", ordersMap);
        } else {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, expected court id = 150, was = %s",
                migrationId, caseDetails.getId(), orders.isEmpty() ? "null" : orders.get().get("court")
            ));
        }
    }

    private void run1371(CaseDetails caseDetails) {
        String migrationId = "DFPL-1371";
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), List.of(1667466628958196L), migrationId);
        caseDetails.getData().putAll(
            migrateCaseService.removeJudicialMessage(getCaseData(caseDetails), migrationId,
                "c6d4ed7b-ca76-47ea-87b7-9538762bab00"));
    }

    private void run1380(CaseDetails caseDetails) {
        var migrationId = "DFPL-1380";
        var possibleCaseIds = List.of(1662460879255241L);
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        caseDetails.getData().put("state", State.FINAL_HEARING);
    }

    private void run1437(CaseDetails caseDetails) {
        var migrationId = "DFPL-1437";

        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), List.of(1680258979928537L), migrationId);
        caseDetails.getData().putAll(migrateCaseService.removeFurtherEvidenceSolicitorDocuments(
            getCaseData(caseDetails), migrationId, UUID.fromString("97d51061-4ca1-4af6-94da-61160a681e2f")));
        caseDetails.getData().putAll(migrateCaseService.removeHearingFurtherEvidenceDocuments(getCaseData(caseDetails),
            migrationId,
            UUID.fromString("c696b0d4-b11b-492e-bcbb-4142d5e47258"),
            UUID.fromString("94161ba0-c229-453d-a6ce-06228aa4cf66")));
        caseDetails.getData().putAll(migrateCaseService.refreshDocumentViews(getCaseData(caseDetails)));
    }

    @SuppressWarnings("unchecked")
    private void run1242(CaseDetails caseDetails) {
        var migrationId = "DFPL-1242";

        var invalidOrderType = "EDUCATION_SUPERVISION__ORDER";
        var validOrderType = "EDUCATION_SUPERVISION_ORDER";

        caseDetails.getData().putAll(migrateCaseService.fixOrderTypeTypo(migrationId, caseDetails));
    }

    private void run1451(CaseDetails caseDetails) {
        var migrationId = "DFPL-1451";
        var possibleCaseIds = List.of(1669909306379829L);
        final UUID expectedOrderId = UUID.fromString("c93a824b-75ce-4ffd-ad30-ad7f42c01ed9");
        final UUID expectedHearingOrderBundleId = UUID.fromString("ebdf7ea7-a2e8-4296-be98-109b9070348e");
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        caseDetails.getData().putAll(migrateCaseService.removeDraftUploadedCMOs(getCaseData(caseDetails),
            migrationId, expectedOrderId));
        caseDetails.getData().putAll(migrateCaseService.removeHearingOrdersBundlesDrafts(getCaseData(caseDetails),
            migrationId, expectedHearingOrderBundleId));
    }
}
