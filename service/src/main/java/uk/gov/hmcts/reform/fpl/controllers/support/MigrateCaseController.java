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
    private static final String PLACEMENT = "placements";
    private static final String PLACEMENT_NON_CONFIDENTIAL = "placementsNonConfidential";
    private static final String PLACEMENT_NON_CONFIDENTIAL_NOTICES = "placementsNonConfidentialNotices";

    private final MigrateCaseService migrateCaseService;

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-1262", this::run1262,
        "DFPL-1274", this::run1274,
        "DFPL-1277", this::run1277,
        "DFPL-1290", this::run1290,
        "DFPL-1294", this::run1294,
        "DFPL-1238", this::run1238,
        "DFPL-1241", this::run1241,
        "DFPL-1244", this::run1244,
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

    private void run1262(CaseDetails caseDetails) {
        var migrationId = "DFPL-1262";
        var possibleCaseIds = List.of(1651753104228873L);
        final UUID placementToRemove = UUID.fromString("195e9334-a308-4992-a890-8d6c8643dc1f");
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        caseDetails.getData().putAll(migrateCaseService.removeSpecificPlacements(getCaseData(caseDetails),
            placementToRemove));
    }

    private void run1274(CaseDetails caseDetails) {
        var migrationId = "DFPL-1274";
        var possibleCaseIds = List.of(1665570034617577L);
        final UUID placementToRemove = UUID.fromString("91217531-42de-4f1c-99b7-aded7233d832");
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        caseDetails.getData().putAll(migrateCaseService.removeSpecificPlacements(getCaseData(caseDetails),
            placementToRemove));
    }

    private void run1277(CaseDetails caseDetails) {
        var migrationId = "DFPL-1277";
        var possibleCaseIds = List.of(1659933720451883L);
        final UUID placementToRemove = UUID.fromString("f1b6d2d8-e960-4b36-a9ae-56723c25ac31");
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        caseDetails.getData().putAll(migrateCaseService.removeSpecificPlacements(getCaseData(caseDetails),
            placementToRemove));
    }

    private void run1290(CaseDetails caseDetails) {
        var migrationId = "DFPL-1290";
        var possibleCaseIds = List.of(1644931377783283L);
        final UUID placementToRemove = UUID.fromString("959bd38f-72d9-42ef-b01d-e5b02aabacfa");
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        caseDetails.getData().putAll(migrateCaseService.removeSpecificPlacements(getCaseData(caseDetails),
            placementToRemove));
    }

    private void run1294(CaseDetails caseDetails) {
        var migrationId = "DFPL-1294";
        var possibleCaseIds = List.of(1676971632816123L);
        final UUID expectedPositionStatementId = UUID.fromString("d74874b8-3ee3-4f06-8e01-a86209ffa31e");
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        caseDetails.getData().putAll(migrateCaseService.removePositionStatementChild(getCaseData(caseDetails),
            migrationId, expectedPositionStatementId));
    }

    private void run1263(CaseDetails caseDetails) {
        var migrationId = "DFPL-1263";
        var possibleCaseIds = List.of(1661171715678642L);
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        caseDetails.getData().putAll(migrateCaseService.renameApplicationDocuments(getCaseData(caseDetails)));
    }
}
