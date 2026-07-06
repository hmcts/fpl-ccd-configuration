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
import uk.gov.hmcts.reform.fpl.service.CaseAccessService;
import uk.gov.hmcts.reform.fpl.service.MigrateCaseService;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;

import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;

@Slf4j
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MigrateCaseController extends CallbackController {
    public static final String MIGRATION_ID_KEY = "migrationId";
    private final MigrateCaseService migrateCaseService;
    private final CaseAccessService caseAccessService;

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-log", this::runLog,
        "DFPL-3290", this::run3290
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

    private void run3290(CaseDetails caseDetails) {
        final String migrationId = "DFPL-3290";
        final long expectedCaseId = 1780995216446125L;

        Long caseId = caseDetails.getId();
        migrateCaseService.doCaseIdCheck(caseId, expectedCaseId, migrationId);

        caseAccessService.grantCaseAccess(caseId, Set.of(
            "76d29d26-931f-452e-ae8f-dda550aaf505",
            "b479e1ef-489f-4cfe-8c27-7ce2b290ddb5",
            "99ba1478-02ad-4449-8a9b-fb9165bf25b3",
            "47fcc1ed-40a9-41b7-bda3-2a44b9e16247"
        ), LASOLICITOR);

    }
}
