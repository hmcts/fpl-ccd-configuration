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
    private final DfjAreaLookUpService dfjAreaLookUpService;

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-1124", this::run1124,
        "DFPL-1124Rollback", this::run1124Rollback,
        "DFPL-1262", this::run1262,
        "DFPL-1274", this::run1274,
        "DFPL-1277", this::run1277,
        "DFPL-1290", this::run1290,
        "DFPL-1294", this::run1294
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
}
