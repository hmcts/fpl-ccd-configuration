package uk.gov.hmcts.reform.fpl.controllers.support;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApiV2;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.CaseLocation;
import uk.gov.hmcts.reform.fpl.controllers.CallbackController;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.CourtLookUpService;
import uk.gov.hmcts.reform.fpl.service.DfjAreaLookUpService;
import uk.gov.hmcts.reform.fpl.service.MigrateCaseService;
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrderDocumentScopedFieldsCalculator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static java.lang.String.format;

@Api
@Slf4j
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MigrateCaseController extends CallbackController {
    public static final String MIGRATION_ID_KEY = "migrationId";
    private final CoreCaseDataApiV2 coreCaseDataApi;
    private final RequestData requestData;
    private final AuthTokenGenerator authToken;
    private final CourtLookUpService courtLookUpService;

    private final MigrateCaseService migrateCaseService;
    private final ManageOrderDocumentScopedFieldsCalculator fieldsCalculator;
    private final DfjAreaLookUpService dfjAreaLookUpService;

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-1501", this::run1616,
        "DFPL-1584", this::run1612,
        "DFPL-702", this::run702,
        "DFPL-702rollback", this::run702rollback,
        "DFPL-1486", this::run1486,
        "DFPL-1681", this::run1681,
        "DFPL-1663", this::run1663,
        "DFPL-1701", this::run1701,
        "DFPL-CFV", this::runCFV,
        "DFPL-CFV-Rollback", this::runCFVrollback
    );

    private void runCFV(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        migrateCaseService.doHasCFVMigratedCheck(caseDetails.getId(), (String) caseDetails.getData()
                .get("hasBeenCFVMigrated"), "DFPL-CFV");
        caseDetails.getData().putAll(migrateCaseService.migrateApplicantWitnessStatements(caseData));
        caseDetails.getData().putAll(migrateCaseService
            .migrateApplicationDocumentsToDocumentsFiledOnIssueList(caseData));
        caseDetails.getData().putAll(migrateCaseService.migrateApplicationDocumentsToThresholdList(caseData));
        caseDetails.getData().putAll(migrateCaseService.migrateApplicationDocumentsToCarePlanList(caseData));
        caseDetails.getData().putAll(migrateCaseService.migrateCourtBundle(caseData));
        caseDetails.getData().putAll(migrateCaseService.migrateCorrespondenceDocuments(caseData));
        caseDetails.getData().putAll(migrateCaseService.migrateExpertReports(caseData));
        caseDetails.getData().putAll(migrateCaseService.migrateGuardianReports(caseData));
        caseDetails.getData().putAll(migrateCaseService.migrateNoticeOfActingOrIssue(caseData));
        caseDetails.getData().putAll(migrateCaseService.migratePositionStatementRespondent(caseData));
        caseDetails.getData().putAll(migrateCaseService.migratePositionStatementChild(caseData));
        caseDetails.getData().putAll(migrateCaseService.migrateRespondentStatement(caseData));
        caseDetails.getData().putAll(migrateCaseService.migrateSkeletonArgumentList(caseData));
        caseDetails.getData().putAll(migrateCaseService
            .moveCaseSummaryWithConfidentialAddressToCaseSummaryListLA(caseData));
        caseDetails.getData().put("hasBeenCFVMigrated", YesNo.YES);
    }

    private void runCFVrollback(CaseDetails caseDetails) {
        migrateCaseService.doHasCFVMigratedCheck(caseDetails.getId(), (String) caseDetails.getData()
                .get("hasBeenCFVMigrated"), "DFPL-CFV", true);
        migrateCaseService.rollbackApplicantWitnessStatements(caseDetails);
        migrateCaseService.rollbackApplicationDocuments(caseDetails);
        migrateCaseService.rollbackCaseSummaryMigration(caseDetails);
        migrateCaseService.rollbackCourtBundleMigration(caseDetails);
        migrateCaseService.rollbackCorrespondenceDocuments(caseDetails);
        migrateCaseService.rollbackExpertReports(caseDetails);
        migrateCaseService.rollbackGuardianReports(caseDetails);
        migrateCaseService.rollbackNoticeOfActingOrIssue(caseDetails);
        migrateCaseService.rollbackPositionStatementChild(caseDetails);
        migrateCaseService.rollbackPositionStatementRespondent(caseDetails);
        migrateCaseService.rollbackSkeletonArgumentList(caseDetails);
        caseDetails.getData().put("hasBeenCFVMigrated", YesNo.NO);
    }

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

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        final CaseData caseData = getCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        // update supplementary data
        String caseId = caseData.getId().toString();
        Map<String, Map<String, Map<String, Object>>> supplementaryData = new HashMap<>();
        supplementaryData.put("supplementary_data_updates",
            Map.of("$set", Map.of("HMCTSServiceId", "ABA3")));
        coreCaseDataApi.submitSupplementaryData(requestData.authorisation(),
            authToken.generate(), caseId, supplementaryData);

        caseDetails.getData().remove(MIGRATION_ID_KEY);
    }

    private void run702(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        var caseId = caseData.getId();
        String caseName = caseData.getCaseName();

        String courtCode = null;
        if (caseData.getOrders() != null && StringUtils.isNotEmpty(caseData.getOrders().getCourt())) {
            courtCode = caseData.getOrders().getCourt();
        } else if (caseData.getCourt() != null) {
            courtCode = caseData.getCourt().getCode();
        }
        if (courtCode == null) {
            throw new AssertionError(format("Migration {id = DFPL-702, case reference = {}, case state = {}} "
                + "doesn't have court info so unable to set caseManagementLocation "
                + "which is mandatory in global search.", caseId, caseData.getState().getValue()));
        }

        // migrating top level fields: case names
        Optional<Court> lookedUpCourt = courtLookUpService.getCourtByCode(courtCode);
        if (lookedUpCourt.isPresent()) {
            caseDetails.getData().put("caseManagementLocation", CaseLocation.builder()
                .baseLocation(lookedUpCourt.get().getEpimmsId())
                .region(lookedUpCourt.get().getRegionId())
                .build());

            caseDetails.getData().put("caseNameHmctsInternal", caseName);
            caseDetails.getData().put("caseManagementCategory", DynamicList.builder()
                .value(DynamicListElement.builder().code("FPL").label("Family Public Law").build())
                .listItems(List.of(
                    DynamicListElement.builder().code("FPL").label("Family Public Law").build()
                ))
                .build());
        } else {
            throw new AssertionError(format("Migration {id = DFPL-702, case reference = {}, case state = {}} fail to "
                + "lookup ePIMMS ID for court {}", caseId, caseData.getState().getValue(), courtCode));
        }
    }

    private void run702rollback(CaseDetails caseDetails) {
        caseDetails.getData().remove("caseManagementLocation");
        caseDetails.getData().remove("caseNameHmctsInternal");
        caseDetails.getData().remove("caseManagementCategory");
    }

    private void run1612(CaseDetails caseDetails) {
        var migrationId = "DFPL-1612";
        var possibleCaseIds = List.of(1687780363265112L);
        UUID documentId = UUID.fromString("db163749-7c8a-45fe-88dd-63641560a9d9");
        CaseData caseData = getCaseData(caseDetails);
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        migrateCaseService.verifyUrgentDirectionsOrderExists(caseData, migrationId, documentId);
        caseDetails.getData().remove("urgentDirectionsOrder");
    }

    private void run1616(CaseDetails caseDetails) {
        var migrationId = "DFPL-1616";
        var possibleCaseIds = List.of(1687526651029623L);
        UUID documentId = UUID.fromString("528bd6a2-3221-4edb-8dc6-f8060937d443");
        CaseData caseData = getCaseData(caseDetails);
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        migrateCaseService.verifyUrgentDirectionsOrderExists(caseData, migrationId, documentId);
        caseDetails.getData().remove("urgentDirectionsOrder");
    }

    private void run1486(CaseDetails caseDetails) {
        var migrationId = "DFPL-1486";
        caseDetails.getData().putAll(migrateCaseService.addRelatingLA(migrationId, caseDetails.getId()));
    }

    private void run1681(CaseDetails caseDetails) {
        var migrationId = "DFPL-1681";
        var possibleCaseIds = List.of(1669737648667050L);
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);

        caseDetails.getData().remove("correspondenceDocumentsNC");
    }

    private void run1663(CaseDetails caseDetails) {
        var migrationId = "DFPL-1663";
        var possibleCaseIds = List.of(1673973434416600L);
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        caseDetails.getData().put("state", State.CLOSED);
    }

    private void run1701(CaseDetails caseDetails) {
        var migrationId = "DFPL-1701";
        var possibleCaseIds = List.of(1691595070128997L);
        UUID expectedDocument = UUID.fromString("41803670-2ef1-485e-b842-1896b572600b");
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);

        caseDetails.getData().putAll(migrateCaseService.removeApplicationDocument(getCaseData(caseDetails),
            migrationId, expectedDocument));
    }
}
