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

import java.util.List;
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

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-log", this::runLog,
        "DFPL-3390", this::run3390,
        "DFPL-3373", this::run3373,
        "DFPL-3419", this::run3419
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

    public void run3419(CaseDetails caseDetails) {
        final CaseData caseData = getCaseData(caseDetails);
        String orgName = "Wakefield Metropolitan District Council";
        String newOrgName = "Kirklees Council";
        String orgId = "T1GWH7Y";
        String newOrgId = "4001R46";
        String laCode = "KKL";
        List<UUID> bundleIds = List.of(
            UUID.fromString("727006c8-cca7-4c93-88b2-f5bc2aa216ab"),
            UUID.fromString("111462e4-bfba-4db4-900a-8ec1bf9bbc9b")
        );
        String caseRole = CaseRole.SOLICITORA.formattedName();
        int policyIndex = 0;

        final String migrationId = "DFPL-3419";
        final long expectedCaseId = 1783943081734149L;

        migrateCaseService.doCaseIdCheck(caseDetails.getId(), expectedCaseId, migrationId);

        caseDetails.getData().putAll(migrateCaseService.updateRespondents(caseData, orgName,
                                                                          newOrgName, orgId, newOrgId));
        caseDetails.getData().putAll(migrateCaseService.updateRespondentPolicy(caseData, newOrgId,
                                                                               caseRole, policyIndex));
        caseDetails.getData().putAll(migrateCaseService.updateCaseLocalAuthority(newOrgName, laCode));
        caseDetails.getData().putAll(migrateCaseService.updateRespondentLA(caseData, newOrgName, newOrgId));
        caseDetails.getData().putAll(migrateCaseService.updateAdditionalApplications(caseData, newOrgName, orgName,
                                                                                     newOrgId, orgId, bundleIds));
        caseDetails.getData().putAll(migrateCaseService.updateRelatingLA(laCode));
    }

    public void run3390(CaseDetails caseDetails) {
        final CaseData caseData = getCaseData(caseDetails);
        final String orgId = "CS35UMJ";
        final String migrationId = "DFPL-3390";
        final long expectedCaseId = 1786619265255294L;

        migrateCaseService.doCaseIdCheck(caseDetails.getId(), expectedCaseId, migrationId);

        caseDetails.getData().putAll(migrateCaseService.updateOutsourcingPolicy(caseData, orgId, null));
    }

    private void run3373(CaseDetails caseDetails) {
        final String migrationId = "DFPL-3373";
        final long expectedCaseId = 1783093091738688L;
        final String orgId = "L3HSA4L";

        migrateCaseService.doCaseIdCheck(caseDetails.getId(), expectedCaseId, migrationId);

        caseDetails.getData().putAll(migrateCaseService.updateOutsourcingPolicy(
            getCaseData(caseDetails),
            orgId,
            CaseRole.EPSMANAGING.formattedName()
        ));
    }
}

