package uk.gov.hmcts.reform.fpl.controllers.support;

import io.swagger.annotations.Api;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.CallbackController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.removeorder.CMORemovalAction;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@Api
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {
    private final CMORemovalAction cmoRemovalAction;
    private static final String MIGRATION_ID_KEY = "migrationId";

    private final CoreCaseDataService coreCaseDataService;

    private final List<Migration> migrations = List.of(
        new Migration("FPLA-2637", 1605801786538684L, this::run2637),
        new Migration("FPLA-2640", 1603465940779147L, this::run2640)
    );

    @Secured("caseworker-publiclaw-systemupdate")
    @PostMapping(value = "/support/case/migration/{migrationId}", consumes = APPLICATION_JSON_VALUE)
    public void migrateCase(@PathVariable("migrationId") String migrationId) {

        Optional<Migration> migration = getMigration(migrationId);

        if (migration.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, format("Migration %s not found", migrationId));
        }
        coreCaseDataService.triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            migration.get().caseId,
            "migrateCase",
            Map.of(MIGRATION_ID_KEY, migrationId));
    }

    @PostMapping("/callback/migrate-case/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Object migrationId = caseDetails.getData().get(MIGRATION_ID_KEY);

        Optional<Migration> migration = getMigration(migrationId);

        if (migration.isEmpty()) {
            return respond(caseDetails, List.of(format("Migration %s not found", migrationId)));
        }

        migration.get().migration.accept(caseDetails);

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run2637(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("LE20C50024".equals(caseData.getFamilyManCaseNumber())) {
            removeFirstDraftCaseManagementOrder(caseDetails);
        }
    }

    private void run2640(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("NE20C50006".equals(caseData.getFamilyManCaseNumber())) {
            removeFirstDraftCaseManagementOrder(caseDetails);
        }
    }

    private void removeFirstDraftCaseManagementOrder(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if (isEmpty(caseData.getDraftUploadedCMOs())) {
            throw new IllegalArgumentException("No draft case management orders in the case");
        }

        Element<CaseManagementOrder> firstDraftCmo = caseData.getDraftUploadedCMOs().get(0);

        cmoRemovalAction.removeDraftCaseManagementOrder(caseData, caseDetails, firstDraftCmo);
    }

    private Optional<Migration> getMigration(Object migrationId) {
        return migrations.stream()
            .filter(migration -> migration.id.equals(migrationId))
            .findFirst();
    }

    @Data
    @Builder
    private static class Migration {
        String id;
        Long caseId;
        Consumer<CaseDetails> migration;
    }
}
