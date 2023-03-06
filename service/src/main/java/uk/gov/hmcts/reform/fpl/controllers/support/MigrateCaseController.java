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
import uk.gov.hmcts.reform.fpl.enums.HearingOptions;
import uk.gov.hmcts.reform.fpl.service.MigrateCaseService;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
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

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-1210", this::run1210,
        "DFPL-1262", this::run1262,
        "DFPL-1274", this::run1274,
        "DFPL-1277", this::run1277,
        "DFPL-1290", this::run1290
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
    
    private void run1210(CaseDetails caseDetails) {
        String migrationId = "DFPL-1210";
        Map<String, Object> caseDetailsData = caseDetails.getData();
        migrateCaseService.doCaseIdCheck(caseDetails.getId(), 1615556003529811L, migrationId);
        migrateCaseService.doHearingOptionCheck(caseDetails.getId(),
            Optional.of((String) caseDetails.getData().get("hearingOption")).orElse(""),
            "EDIT_HEARING", migrationId);
        caseDetailsData.put("hearingOption", HearingOptions.EDIT_PAST_HEARING);
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
}
