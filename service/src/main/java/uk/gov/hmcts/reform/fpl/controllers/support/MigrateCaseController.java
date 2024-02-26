package uk.gov.hmcts.reform.fpl.controllers.support;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.am.model.RoleAssignment;
import uk.gov.hmcts.reform.am.model.RoleCategory;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.CallbackController;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.JudicialUser;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.JudicialService;
import uk.gov.hmcts.reform.fpl.service.MigrateCFVService;
import uk.gov.hmcts.reform.fpl.service.MigrateCaseService;
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrderDocumentScopedFieldsCalculator;
import uk.gov.hmcts.reform.fpl.utils.RoleAssignmentUtils;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole.ALLOCATED_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.LegalAdviserRole.ALLOCATED_LEGAL_ADVISER;

@Api
@Slf4j
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MigrateCaseController extends CallbackController {
    public static final String MIGRATION_ID_KEY = "migrationId";
    private final ManageOrderDocumentScopedFieldsCalculator fieldsCalculator;
    private final MigrateCaseService migrateCaseService;
    private final MigrateCFVService migrateCFVService;
    private final JudicialService judicialService;

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-CFV", this::runCFV,
        "DFPL-CFV-Rollback", this::runCfvRollback,
        "DFPL-CFV-Failure", this::runCfvFailure,
        "DFPL-CFV-dry", this::dryRunCFV,
        "DFPL-1940", this::run1940,
        "DFPL-AM", this::runAM,
        "DFPL-AM-Rollback", this::runAmRollback,
        "DFPL-1882", this::run1882,
        "DFPL-2177", this::run2177,
        "DFPL-2148", this::run2148,
        "DFPL-2149", this::run2149
    );

    private static void pushChangesToCaseDetails(CaseDetails caseDetails, Map<String, Object> changes) {
        for (Map.Entry<String, Object> entrySet : changes.entrySet()) {
            if (entrySet.getValue() == null || (entrySet.getValue() instanceof Collection
                && ((Collection) entrySet.getValue()).isEmpty())) {
                caseDetails.getData().remove(entrySet.getKey());
            } else {
                caseDetails.getData().put(entrySet.getKey(), entrySet.getValue());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void mergeChanges(Map<String, Object> target, Map<String, Object> newChanges) {
        newChanges.entrySet().forEach(entry -> {
            if (target.containsKey(entry.getKey())) {
                ((List) target.get(entry.getKey())).addAll((List) entry.getValue());
            } else {
                target.put(entry.getKey(), entry.getValue());
            }
        });
    }

    private Map<String, Object> prepareChangesForMigratingAllToArchivedDocuments(String migrationId,
                                                                                 CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        migrateCFVService.doHasCFVMigratedCheck(caseDetails.getId(), (String) caseDetails.getData()
            .get("hasBeenCFVMigrated"), migrationId);
        Map<String, Object> changes = new LinkedHashMap<>();
        mergeChanges(changes, migrateCFVService.migrateHearingFurtherEvidenceDocumentsToArchivedDocuments(caseData));
        mergeChanges(changes, migrateCFVService.migrateFurtherEvidenceDocumentsToArchivedDocuments(caseData));
        mergeChanges(changes, migrateCFVService.migrateCaseSummaryToArchivedDocuments(caseData));
        mergeChanges(changes, migrateCFVService.migratePositionStatementToArchivedDocuments(caseData));
        mergeChanges(changes, migrateCFVService.migrateRespondentStatementToArchivedDocuments(caseData));
        mergeChanges(changes, migrateCFVService.migrateCorrespondenceDocumentsToArchivedDocuments(caseData));
        mergeChanges(changes, migrateCFVService.migrateApplicationDocumentsToArchivedDocuments(caseData));
        mergeChanges(changes, migrateCFVService.migrateCourtBundlesToArchivedDocuments(caseData));
        changes.put("hasBeenCFVMigrated", YesNo.YES);
        return changes;
    }

    private Map<String, Object> prepareChangesForCFVMigration(String migrationId, CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        migrateCFVService.doHasCFVMigratedCheck(caseDetails.getId(), (String) caseDetails.getData()
            .get("hasBeenCFVMigrated"), migrationId);
        Map<String, Object> changes = new LinkedHashMap<>();
        changes.putAll(migrateCFVService.migrateApplicantWitnessStatements(caseData));
        changes.putAll(migrateCFVService.migrateApplicationDocuments(caseData));
        changes.putAll(migrateCFVService.migrateCourtBundle(caseData));
        changes.putAll(migrateCFVService.migrateCorrespondenceDocuments(caseData));
        changes.putAll(migrateCFVService.migrateExpertReports(caseData));
        changes.putAll(migrateCFVService.migrateGuardianReports(caseData));
        changes.putAll(migrateCFVService.migrateNoticeOfActingOrIssue(caseData));
        changes.putAll(migrateCFVService.migrateArchivedDocuments(caseData));
        changes.putAll(migrateCFVService.migratePositionStatementRespondent(caseData));
        changes.putAll(migrateCFVService.migratePositionStatementChild(caseData));
        changes.putAll(migrateCFVService.migrateRespondentStatement(caseData));
        changes.putAll(migrateCFVService.migrateSkeletonArgumentList(caseData));
        changes.putAll(migrateCFVService.moveCaseSummaryWithConfidentialAddressToCaseSummaryListLA(caseData));
        changes.put("hasBeenCFVMigrated", YesNo.YES);
        return changes;
    }

    private void dryRunCFV(CaseDetails caseDetails) {
        var migrationId = "DFPL-CFV-dry";
        CaseData caseData = getCaseData(caseDetails);
        Map<String, Object> changes = prepareChangesForCFVMigration(migrationId, caseDetails);
        migrateCFVService.validateMigratedNumberOfDocuments(migrationId, caseData, changes);
    }

    private void runCFV(CaseDetails caseDetails) {
        var migrationId = "DFPL-CFV";
        CaseData caseData = getCaseData(caseDetails);
        Map<String, Object> changes = prepareChangesForCFVMigration(migrationId, caseDetails);
        try {
            migrateCFVService.validateMigratedNumberOfDocuments(migrationId, caseData, changes);
        } catch (AssertionError ex) {
            changes = prepareChangesForMigratingAllToArchivedDocuments(migrationId, caseDetails);
        }
        pushChangesToCaseDetails(caseDetails, changes);
    }

    private void runCfvFailure(CaseDetails caseDetails) {
        var migrationId = "DFPL-CFV-Failure";
        CaseData caseData = getCaseData(caseDetails);
        Map<String, Object> changes = prepareChangesForMigratingAllToArchivedDocuments(migrationId,
            caseDetails);
        pushChangesToCaseDetails(caseDetails, changes);
    }

    private void runCfvRollback(CaseDetails caseDetails) {
        migrateCFVService.doHasCFVMigratedCheck(caseDetails.getId(), (String) caseDetails.getData()
                .get("hasBeenCFVMigrated"), "DFPL-CFV-Rollback", true);

        Map<String, Object> changes = new LinkedHashMap<>();
        changes.putAll(migrateCFVService.rollbackApplicantWitnessStatements());
        changes.putAll(migrateCFVService.rollbackApplicationDocuments());
        changes.putAll(migrateCFVService.rollbackCaseSummaryMigration(caseDetails));
        changes.putAll(migrateCFVService.rollbackCourtBundleMigration(caseDetails));
        changes.putAll(migrateCFVService.rollbackCorrespondenceDocuments());
        changes.putAll(migrateCFVService.rollbackExpertReports());
        changes.putAll(migrateCFVService.rollbackGuardianReports());
        changes.putAll(migrateCFVService.rollbackNoticeOfActingOrIssue());
        changes.putAll(migrateCFVService.rollbackRespondentStatement());
        changes.putAll(migrateCFVService.rollbackPositionStatementChild(caseDetails));
        changes.putAll(migrateCFVService.rollbackPositionStatementRespondent(caseDetails));
        changes.putAll(migrateCFVService.rollbackSkeletonArgumentList(caseDetails));
        changes.putAll(migrateCFVService.rollbackArchivedDocumentsList());
        changes.put("hasBeenCFVMigrated", null);
        pushChangesToCaseDetails(caseDetails, changes);
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

    private void run1940(CaseDetails caseDetails) {
        var migrationId = "DFPL-1940";
        var possibleCaseIds = List.of(1697791879605293L);
        var expectedMessageId = UUID.fromString("29b3eab8-1e62-4aa2-86d1-17874d27933e");

        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        CaseData caseData = getCaseData(caseDetails);
        caseDetails.getData().putAll(migrateCaseService.removeJudicialMessage(caseData, migrationId,
            String.valueOf(expectedMessageId)));
    }

    private void run2177(CaseDetails caseDetails) {
        var migrationId = "DFPL-2177";
        var possibleCaseIds = List.of(1704384343011099L);
        var expectedDocumentId = UUID.fromString("8e5cf45c-98d0-45f7-851a-974b6afbdb44");

        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        CaseData caseData = getCaseData(caseDetails);
        migrateCaseService.verifyUrgentDirectionsOrderExists(caseData, migrationId, expectedDocumentId);
        caseDetails.getData().remove("urgentDirectionsOrder");
    }

    private void migrateRoles(CaseData caseData) {
        List<RoleAssignment> rolesToAssign = new ArrayList<>();

        // If we have an allocated judge with an IDAM ID (added in about-to-submit step from mapping)
        Optional<Judge> allocatedJudge = judicialService.getAllocatedJudge(caseData);
        if (allocatedJudge.isPresent()
            && !isEmpty(allocatedJudge.get().getJudgeJudicialUser())
            && !isEmpty(allocatedJudge.get().getJudgeJudicialUser().getIdamId())) {

            Optional<RoleCategory> userRoleCategory = judicialService
                .getUserRoleCategory(allocatedJudge.get().getJudgeEmailAddress());

            if (userRoleCategory.isPresent()) {
                // attempt to assign allocated-[role]
                boolean isLegalAdviser = userRoleCategory.get().equals(RoleCategory.LEGAL_OPERATIONS);

                rolesToAssign.add(RoleAssignmentUtils.buildRoleAssignment(
                    caseData.getId(),
                    allocatedJudge.get().getJudgeJudicialUser().getIdamId(),
                    isLegalAdviser ? ALLOCATED_LEGAL_ADVISER.getRoleName() : ALLOCATED_JUDGE.getRoleName(),
                    userRoleCategory.get(),
                    ZonedDateTime.now(),
                    null // no end date
                ));
            }
        } else {
            log.error("Could not assign allocated-judge on case {}, no UUID found on the case", caseData.getId());
        }

        // get hearing judge roles to add (if any)
        rolesToAssign.addAll(judicialService.getHearingJudgeRolesForMigration(caseData));

        log.info("Attempting to create {} roles on case {}", rolesToAssign.size(), caseData.getId());
        judicialService.migrateJudgeRoles(rolesToAssign);
    }

    private void runAmRollback(CaseDetails caseDetails) {
        var migrationId = "DFPL-AM-Rollback";
        CaseData caseData = getCaseData(caseDetails);

        Judge allocatedJudge = caseData.getAllocatedJudge();
        if (!isEmpty(allocatedJudge)) {
            caseDetails.getData().put("allocatedJudge", allocatedJudge.toBuilder()
                .judgeEnterManually(null)
                .judgeJudicialUser(null)
                .build());
        }

        List<Element<HearingBooking>> hearingsWithIdamIdsStripped = caseData.getAllNonCancelledHearings()
            .stream().map(hearing -> {
                HearingBooking booking = hearing.getValue();

                booking.setJudgeAndLegalAdvisor(booking.getJudgeAndLegalAdvisor().toBuilder()
                    .judgeEnterManually(null)
                    .judgeJudicialUser(null)
                    .build());
                hearing.setValue(booking);
                return hearing;
            }).toList();

        if (caseData.getAllNonCancelledHearings().size() > 0) {
            caseDetails.getData().put("hearingDetails", hearingsWithIdamIdsStripped);
        }
        caseDetails.getData().remove("hasBeenAMMigrated");

        // delete all roles on the case - if this fails we WANT the migration to stop, as it has not been rolled back
        judicialService.deleteAllRolesOnCase(caseData.getId());
    }

    private void runAM(CaseDetails caseDetails) {
        var migrationId = "DFPL-AM";

        CaseData caseData = getCaseData(caseDetails);

        // 1. cleanup from past migration
        judicialService.deleteAllRolesOnCase(caseData.getId());

        // 2. Add UUIDs for judges + legal advisers
        Judge allocatedJudge = caseData.getAllocatedJudge();
        if (!isEmpty(allocatedJudge) && !isEmpty(allocatedJudge.getJudgeEmailAddress())) {
            String email = allocatedJudge.getJudgeEmailAddress();
            Optional<String> uuid = judicialService.getJudgeUserIdFromEmail(email);
            // add the UUID to the allocated judge and save on the case

            if (uuid.isEmpty()) {
                log.info("Could not find judge UUID, caseId={}, allocatedJudge, ejudiciary={}, judgeTitle={}",
                    caseData.getId(), email.toLowerCase().endsWith("@ejudiciary.net"), allocatedJudge.getJudgeTitle());
            }

            uuid.ifPresent(s -> caseDetails.getData().put("allocatedJudge", allocatedJudge.toBuilder()
                .judgeJudicialUser(JudicialUser.builder()
                    .idamId(s)
                    .build())
                .build()));
        }

        List<Element<HearingBooking>> hearings = caseData.getAllNonCancelledHearings();
        List<Element<HearingBooking>> modified = hearings.stream()
            .map(el -> {
                HearingBooking val = el.getValue();
                if (!isEmpty(val.getJudgeAndLegalAdvisor())
                    && !isEmpty(val.getJudgeAndLegalAdvisor().getJudgeEmailAddress())) {

                    String email = val.getJudgeAndLegalAdvisor().getJudgeEmailAddress();
                    Optional<String> uuid = judicialService.getJudgeUserIdFromEmail(email);
                    if (uuid.isPresent()) {
                        el.setValue(val.toBuilder()
                            .judgeAndLegalAdvisor(val.getJudgeAndLegalAdvisor().toBuilder()
                                .judgeJudicialUser(JudicialUser.builder()
                                    .idamId(uuid.get())
                                    .build())
                                .build())
                            .build());
                        return el;
                    } else {
                        log.info("Could not find judge UUID, caseId={}, hearingId={}, ejudiciary={}, judgeTitle={}",
                            caseData.getId(), el.getId(), email.toLowerCase().endsWith("@ejudiciary.net"),
                            val.getJudgeAndLegalAdvisor().getJudgeTitle());
                    }
                }
                return el;
            }).toList();

        if (!hearings.isEmpty()) {
            // don't add an empty array if there weren't any hearings beforehand
            caseDetails.getData().put("hearingDetails", modified);
        }
        caseDetails.getData().put("hasBeenAMMigrated", "Yes");

        // Convert our newly annotated case details payload to a case data object
        CaseData newCaseData = getCaseData(caseDetails);

        // 3. Attempt to assign the new roles in AM
        migrateRoles(newCaseData);
    }

    private void run1882(CaseDetails caseDetails) {
        var migrationId = "DFPL-1882";

        CaseData caseData = getCaseData(caseDetails);
        caseDetails.getData().putAll(migrateCaseService.migrateCaseRemoveUnknownAllocatedJudgeTitle(caseData,
            migrationId));
    }

    private void run2148(CaseDetails caseDetails) {
        var migrationId = "DFPL-2148";
        var possibleCaseIds = List.of(1706286062107610L);
        UUID expectedDocumentFiledOnIssueId = UUID.fromString("22c72f17-76e4-4e9f-b76c-221f6ca7b029");
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);

        caseDetails.getData().putAll(migrateCaseService.removeDocumentFiledOnIssue(getCaseData(caseDetails),
            expectedDocumentFiledOnIssueId, migrationId));
    }

    private void run2149(CaseDetails caseDetails) {
        var migrationId = "DFPL-2149";
        var possibleCaseIds = List.of(1689246804719172L);
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);

        CaseData caseData = getCaseData(caseDetails);
        UUID returnApplicationDocId = UUID.fromString("22c72f17-76e4-4e9f-b76c-221f6ca7b029");

        migrateCaseService.verifyReturnApplicationExists(caseData, migrationId, returnApplicationDocId);
        caseDetails.getData().remove("urgentDirectionsOrder");
    } 
}
