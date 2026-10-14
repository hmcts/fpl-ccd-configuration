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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.CaseAccessService;
import uk.gov.hmcts.reform.fpl.service.MigrateCaseService;

import java.util.Map;
import java.util.NoSuchElementException;
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
        "DFPL-2423", this::run2423,
        "DFPL-2423-rollback", this::run2423Rollback,
        "DFPL-3213-v2", this::run3213v2,
        "DFPL-3361", this::run3361,
        "DFPL-3017", this::run3017
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

    private void run2423(CaseDetails caseDetails) {
        final String migrationId = "DFPL-2423";

        migrateCaseService.migrateOtherProceedings(caseDetails, getCaseData(caseDetails), migrationId);
    }

    private void run2423Rollback(CaseDetails caseDetails) {
        final String migrationId = "DFPL-2423-rollback";
        migrateCaseService.rollbackOtherProceedings(caseDetails, getCaseData(caseDetails), migrationId);
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

    private void run3374(CaseDetails caseDetails) {
        final String migrationId = "DFPL-3374";
        final long expectedCaseId = 1784796259751728L;
        final String outsourcingOrgId = "CPYYWBZ";
        Long caseId = caseDetails.getId();

        migrateCaseService.doCaseIdCheck(caseId, expectedCaseId, migrationId);

        caseDetails.getData().putAll(migrateCaseService
            .updateOutsourcingPolicy(getCaseData(caseDetails), outsourcingOrgId, null));
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

    private void run3361(CaseDetails caseDetails) {
        final String migrationId = "DFPL-3361";
        final long expectedCaseId = 1761903519622353L;
        final String orgId = "CPYYWBZ";

        Long caseId = caseDetails.getId();
        migrateCaseService.doCaseIdCheck(caseId, expectedCaseId, migrationId);

        caseDetails.getData().putAll(migrateCaseService.updateOutsourcingPolicy(getCaseData(caseDetails),
            orgId, CaseRole.EPSMANAGING.formattedName()));

    }

    public void run3363(CaseDetails caseDetails) {
        final String migrationId = "DFPL-3363";
        final CaseData caseData = getCaseData(caseDetails);
        caseDetails.getData().putAll(migrateCaseService.updateChildStatusWithFinalOrderIssued(migrationId, caseData));
    }

    public void run3017(CaseDetails caseDetails) {
        final CaseData caseData = getCaseData(caseDetails);
        String orgId = "KB2TFNA";
        String caseRole = CaseRole.LAMANAGING.formattedName();
        String orgName = "Milton Keynes Council";
        String newOrgName = "Milton Keynes City Council";

        caseDetails.getData().putAll(migrateCaseService.updateLocalAuthorityPolicy(caseData, orgId,
                                                                                   caseRole, newOrgName));
        caseDetails.getData().putAll(migrateCaseService.updateLocalAuthority(caseData, orgName,  newOrgName));
        caseDetails.getData().putAll(migrateCaseService.updateCaseLocalAuthorityName(newOrgName));
    }
}

