package uk.gov.hmcts.reform.fpl.controllers.support;

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
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.service.CaseAccessService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.MigrateCaseService;

import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MigrateCaseController extends CallbackController {
    public static final String MIGRATION_ID_KEY = "migrationId";
    private final MigrateCaseService migrateCaseService;
    private final CaseAccessService caseAccessService;
    private final FeatureToggleService featureToggleService;

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-log", this::runLog,
        "DFPL-2284", this::run2284,
        "DFPL-2339", this::run2339,
        "DFPL-2343", this::run2343
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

    private void runLog(CaseDetails caseDetails) {
        log.info("Logging migration on case {}", caseDetails.getId());
    }

    private void run2284(CaseDetails caseDetails) {
        caseDetails.getData().putAll(
            migrateCaseService.changeThirdPartyStandaloneApplicant(getCaseData(caseDetails), "5ZZ1FJX"));

        // Remove the user roles
        String idsToRemove = featureToggleService.getUserIdsToRemoveRolesFrom();
        if (!idsToRemove.isBlank()) {
            Arrays.stream(idsToRemove.split(";")).forEach(id -> {
                caseAccessService.revokeCaseRoleFromUser(
                    caseDetails.getId(), id, CaseRole.SOLICITORA);
            });
        }
    }

    private void run2339(CaseDetails caseDetails) {
        final String migrationId = "DFPL-2339";
        final long expectedCaseId = 1706780490728419L;
        final String orgId = "CPYYWBZ";
        migrateCaseService.doCaseIdCheck(caseDetails.getId(), expectedCaseId, migrationId);

        caseDetails.getData().putAll(migrateCaseService.changeThirdPartyStandaloneApplicant(getCaseData(caseDetails),
            orgId));
    }

    private void run2343(CaseDetails caseDetails) {
        final String migrationId = "DFPL-2343";
        final long expectedCaseId = 1702981005668215L;
        migrateCaseService.doCaseIdCheck(caseDetails.getId(), expectedCaseId, migrationId);

        caseDetails.getData().putAll(migrateCaseService.removeAdditionalApplicationBundle(getCaseData(caseDetails),
            UUID.fromString("cd54e598-27cf-411b-8f8a-a3b1b26b586d"), migrationId));
    }
}
