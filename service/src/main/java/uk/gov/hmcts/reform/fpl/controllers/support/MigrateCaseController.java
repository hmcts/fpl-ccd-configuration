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
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.controllers.CallbackController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.CaseAccessService;
import uk.gov.hmcts.reform.fpl.service.MigrateCaseService;

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
    private static final String FLEETWOOD_EPIMMS_ID = "401452";
    private static final String BLACKPOOL_EPIMMS_ID = "214320";
    private static final String BLACKPOOL_COURT_CODE = "131";
    private static final String BLACKPOOL_COURT_NAME = "Family Court sitting at Blackpool";
    private static final String BLACKBURN_LANCASTER_DFJ_COURT = "blackburnLancasterDFJCourt";

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-log", this::runLog,
        "DFPL-3213", this::run3213,
        "DFPL-2421", this::run2421,
        "DFPL-2421-rollback", this::rollback2421,
        "DFPL-3363", this::run3363,
        "DFPL-3213-v2", this::run3213v2,
        "DFPL-3345", this::run3345
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

    private void run2421(CaseDetails caseDetails) {
        final String migrationId = "DFPL-2421";
        migrateCaseService.migrateOthersToOthersV2(getCaseData(caseDetails), caseDetails.getData(), migrationId);
    }

    private void rollback2421(CaseDetails caseDetails) {
        final String migrationId = "DFPL-2421-rollback";
        migrateCaseService.rollbackOthersV2ToOthers(getCaseData(caseDetails), caseDetails.getData(), migrationId);
    }

    //run 3213 Migrate function to replace Fleetwood Location with BlackPool Location
    private void run3213(CaseDetails caseDetails) {
        final String migrationId = "DFPL-3213";

        Long caseId = caseDetails.getId();
        log.info("Migration {id = {}, case reference = {}} processing", migrationId, caseId);

        CaseData caseData = getCaseData(caseDetails);

        // Calling the service to replace Fleetwood location with Blackpool Location if exists
        caseDetails.getData().putAll(migrateCaseService.updateCaseManagementLocation(
            migrationId,
            caseData,
            FLEETWOOD_EPIMMS_ID,
            BLACKPOOL_EPIMMS_ID,
            BLACKPOOL_COURT_CODE,
            BLACKPOOL_COURT_NAME,
            BLACKBURN_LANCASTER_DFJ_COURT
        ));
    }

    private void run3346(CaseDetails caseDetails) {
        final String DFPL_3346 = "DFPL-3346";
        final long CASE_ID_3346 = 1781013695412110L;
        final String ORG_ID_3346 = "CPYYWBZ";
        runOutsourcingPolicyMigration(caseDetails, DFPL_3346, CASE_ID_3346, ORG_ID_3346);
    }

    private void run3347(CaseDetails caseDetails) {
        final String DFPL_3347 = "DFPL-3347";
        final long CASE_ID_3347 = 1783696286134453L;
        final String ORG_ID_3347 = "ZL7FAG5";
        runOutsourcingPolicyMigration(caseDetails, DFPL_3347, CASE_ID_3347, ORG_ID_3347);
    }

    private void run3345(CaseDetails caseDetails) {
        final String migrationId = "DFPL-3345";
        final long expectedCaseId = 1777371329249951L;
        final UUID targetOrderId = UUID.fromString("13f8bfee-4ed0-40b2-87ac-0300552584d1");

        Long caseId = caseDetails.getId();
        migrateCaseService.doCaseIdCheck(caseId, expectedCaseId, migrationId);

        CaseData caseData = getCaseData(caseDetails);

        Map<String, Object> updatedData = migrateCaseService.removeDraftOrdersRemovedElement(
            caseData,
            migrationId,
            targetOrderId
        );

        caseDetails.getData().putAll(updatedData);
    }

    private void runOutsourcingPolicyMigration(CaseDetails caseDetails, String migrationId,
                                               long expectedCaseId, String targetOrgId) {
        Long caseId = caseDetails.getId();

        migrateCaseService.doCaseIdCheck(caseId, expectedCaseId, migrationId);
        log.info("Migration of {} is started for case {}", migrationId, caseId);

        Map<String, OrganisationPolicy> migrationResult =
            migrateCaseService.updateOutsourcingPolicy(getCaseData(caseDetails), targetOrgId, null);

        caseDetails.getData().putAll(migrationResult);

        OrganisationPolicy updatedOrgPolicy =  migrationResult.get("outsourcingPolicy");
        if (updatedOrgPolicy != null && updatedOrgPolicy.getOrganisation() != null) {
            log.info("Migration {} successfully updated outsourcingPolicy to organisation {} on case {}",
                migrationId, updatedOrgPolicy.getOrganisation().getOrganisationID(), caseId);
        } else {
            log.info("Migration {} completed but outsourcingPolicy was null for case {}", migrationId, caseId);
        }
    }

    // run 3213 Migrate function to replace Fleetwood Location with Preston Location
    private void run3213v2(CaseDetails caseDetails) {
        String migrationId = "DFPL-3213-v2";
        String expectedBaseLocation = "102476";
        String prestonCourtCode = "303";

        Long caseId = caseDetails.getId();
        log.info("Migration {id = {}, case_reference = {}} processing", migrationId, caseId);

        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().putAll(migrateCaseService.updateOrdersCourt(
            migrationId,
            caseData,
            expectedBaseLocation,
            prestonCourtCode
        ));
    }

    public void run3363(CaseDetails caseDetails) {
        final String migrationId = "DFPL-3363";
        final CaseData caseData = getCaseData(caseDetails);
        caseDetails.getData().putAll(migrateCaseService.migrateChildWithFinalOrderIssued(migrationId, caseData));
    }
}

