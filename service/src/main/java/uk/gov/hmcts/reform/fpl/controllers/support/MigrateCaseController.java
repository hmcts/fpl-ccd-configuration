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
import uk.gov.hmcts.reform.fpl.service.DfjAreaLookUpService;
import uk.gov.hmcts.reform.fpl.service.MigrateCaseService;
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrderDocumentScopedFieldsCalculator;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
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
    private final DfjAreaLookUpService dfjAreaLookUpService;

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-1359", this::run1359,
        "DFPL-1401", this::run1401,
        "DFPL-1451", this::run1451,
        "DFPL-1466", this::run1466,
        "DFPL-1501", this::run1501,
        "DFPL-1584", this::run1584,
        "DFPL-1124", this::run1124,
        "DFPL-1124Rollback", this::run1124Rollback
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

    private void run1359(CaseDetails caseDetails) {
        migrateCaseService.doDocumentViewNCCheck(caseDetails.getId(), "DFPL-1359", caseDetails);
        caseDetails.getData().putAll(migrateCaseService.refreshDocumentViews(getCaseData(caseDetails)));
    }

    private void run1401(CaseDetails caseDetails) {
        var migrationId = "DFPL-1401";
        var possibleCaseIds = List.of(1666959378667166L);
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        caseDetails.getData().put("relatingLA", "NCC");
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

    private void run1466(CaseDetails caseDetails) {
        var migrationId = "DFPL-1466";
        var possibleCaseIds = List.of(1665396049325141L);
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);

        caseDetails.getData().putAll(migrateCaseService.removePositionStatementChild(getCaseData(caseDetails),
            migrationId, UUID.fromString("b8da3a48-441f-4210-a21c-7008d256aa32")));
    }

    private void run1501(CaseDetails caseDetails) {
        var migrationId = "DFPL-1501";
        var possibleCaseIds = List.of(1659711594032934L);

        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        caseDetails.getData().putAll(migrateCaseService.removeFurtherEvidenceSolicitorDocuments(
            getCaseData(caseDetails), migrationId, UUID.fromString("43a9287c-f840-4104-958f-cbd98d28aea3")));
    }

    private void run1584(CaseDetails caseDetails) {
        var migrationId = "DFPL-1584";
        var possibleCaseIds = List.of(1666625563479804L);
        final UUID hearingId = UUID.fromString("a9b66732-f85e-490e-a980-01dd7c5f7b36");
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        caseDetails.getData().putAll(migrateCaseService.removeCaseSummaryByHearingId(getCaseData(caseDetails),
            migrationId, hearingId));
    }

    private void run1124(CaseDetails caseDetails) {
        log.info("Migrating case {}", caseDetails.getId());
    }

    private void run1124Rollback(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        var caseId = caseData.getId();
        if (Objects.nonNull(caseData.getDfjArea())) {
            caseDetails.getData().remove("dfjArea");
            caseDetails.getData().keySet().removeAll(dfjAreaLookUpService.getAllCourtFields());
            log.info("Rollback {id = DFPL-1124, case reference = {}} removed dfj area and relevant court field",
                caseId);
        } else {
            log.warn("Rollback {id = DFPL-1124, case reference = {}} doesn't have dfj area and relevant court field",
                caseId);
        }
    }
}
