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

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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
    private static final String PLACEMENT = "placements";
    private static final String PLACEMENT_NON_CONFIDENTIAL = "placementsNonConfidential";
    private static final String PLACEMENT_NON_CONFIDENTIAL_NOTICES = "placementsNonConfidentialNotices";

    private final MigrateCaseService migrateCaseService;
    private final ManageOrderDocumentScopedFieldsCalculator fieldsCalculator;

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-1233", this::run1233,
        "DFPL-1233Rollback", this::run1233Rollback,
        "DFPL-1238", this::run1238,
        "DFPL-1241", this::run1241,
        "DFPL-1244", this::run1244,
        "DFPL-1243", this::run1243,
        "DFPL-1282", this::run1282,
        "DFPL-1270", this::run1270,
        "DFPL-1297", this::run1297,
        "DFPL-1263", this::run1263
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

    private void run1233Rollback(CaseDetails caseDetails) {
        caseDetails.getData().putAll(migrateCaseService.rollbackHearingType(getCaseData(caseDetails)));
    }

    private void run1233(CaseDetails caseDetails) {
        caseDetails.getData().putAll(migrateCaseService.migrateHearingType(getCaseData(caseDetails)));
    }

    private void run1238(CaseDetails caseDetails) {
        var migrationId = "DFPL-1238";
        var possibleCaseIds = List.of(1635423187428763L);

        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);

        caseDetails.getData().remove(PLACEMENT);
        caseDetails.getData().remove(PLACEMENT_NON_CONFIDENTIAL);
        caseDetails.getData().remove(PLACEMENT_NON_CONFIDENTIAL_NOTICES);
    }

    private void run1241(CaseDetails caseDetails) {
        var migrationId = "DFPL-1241";
        var possibleCaseIds = List.of(1652968793683878L);

        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);

        caseDetails.getData().remove(PLACEMENT);
        caseDetails.getData().remove(PLACEMENT_NON_CONFIDENTIAL);
        caseDetails.getData().remove(PLACEMENT_NON_CONFIDENTIAL_NOTICES);
    }

    private void run1244(CaseDetails caseDetails) {
        var migrationId = "DFPL-1244";
        var possibleCaseIds = List.of(1644912253936021L);

        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);

        caseDetails.getData().remove(PLACEMENT);
        caseDetails.getData().remove(PLACEMENT_NON_CONFIDENTIAL);
        caseDetails.getData().remove(PLACEMENT_NON_CONFIDENTIAL_NOTICES);
    }

    private void run1282(CaseDetails caseDetails) {
        var migrationId = "DFPL-1282";
        var expectedCaseId = 1670402369344671L;

        CaseData caseData = getCaseData(caseDetails);

        Long caseId = caseData.getId();
        if (caseId != expectedCaseId) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, expected case id %d",
                migrationId, caseId, expectedCaseId
            ));
        }
        fieldsCalculator.calculate().forEach(caseDetails.getData()::remove);
    }

    private void run1270(CaseDetails caseDetails) {
        String migrationId = "DFPL-1270";
        migrateCaseService.doCaseIdCheck(caseDetails.getId(), 1656581935993992L, migrationId);
        caseDetails.getData().putAll(migrateCaseService.removeCaseSummaryByHearingId(getCaseData(caseDetails),
            migrationId, UUID.fromString("9e6bc008-3577-4c6b-8a29-521c9027b17f")));
    }

    private void run1297(CaseDetails caseDetails) {
        var migrationId = "DFPL-1297";
        var possibleCaseIds = List.of(1675337979577440L);
        final UUID expectedPositionStatementId = UUID.fromString("ea2bd69a-bae9-4707-8bf1-fb6c254b2631");
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        caseDetails.getData().putAll(migrateCaseService.removePositionStatementChild(getCaseData(caseDetails),
            migrationId, expectedPositionStatementId));
    }

    private void run1243(CaseDetails caseDetails) {
        var migrationId = "DFPL-1243";
        var possibleCaseIds = List.of(1675697653441050L);

        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);

        caseDetails.getData().putAll(migrateCaseService.removePositionStatementChild(getCaseData(caseDetails),
            migrationId, UUID.fromString("ffab9b6e-436f-4c7f-afba-ee646b9fb307")));
    }

    private void run1263(CaseDetails caseDetails) {
        var migrationId = "DFPL-1263";
        var possibleCaseIds = List.of(1661171715678642L);
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        caseDetails.getData().putAll(migrateCaseService.renameApplicationDocuments(getCaseData(caseDetails)));
    }
}
