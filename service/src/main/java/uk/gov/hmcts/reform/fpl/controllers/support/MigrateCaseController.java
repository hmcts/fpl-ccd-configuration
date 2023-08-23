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
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApiV2;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.CallbackController;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.JudicialUser;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.CourtLookUpService;
import uk.gov.hmcts.reform.fpl.service.DfjAreaLookUpService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.JudicialService;
import uk.gov.hmcts.reform.fpl.service.MigrateCaseService;
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrderDocumentScopedFieldsCalculator;
import uk.gov.hmcts.reform.fpl.utils.RoleAssignmentUtils;

import java.time.ZonedDateTime;
import java.util.ArrayList;
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
    private final CoreCaseDataApiV2 coreCaseDataApi;
    private final RequestData requestData;
    private final AuthTokenGenerator authToken;
    private final CourtLookUpService courtLookUpService;
    private final FeatureToggleService featureToggleService;

    private final MigrateCaseService migrateCaseService;
    private final ManageOrderDocumentScopedFieldsCalculator fieldsCalculator;
    private final DfjAreaLookUpService dfjAreaLookUpService;
    private final JudicialService judicialService;

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-1359", this::run1359,
        "DFPL-1401", this::run1401,
        "DFPL-1451", this::run1451,
        "DFPL-1501", this::run1616,
        "DFPL-1584", this::run1612,
        "DFPL-1649", this::run1649,
        "DFPL-1486", this::run1486,
        "DFPL-AM", this::runAM,
        "DFPL-AM-Rollback", this::runAmRollback
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

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        if (featureToggleService.isAMMigrationRollbackEnabled()) {
            judicialService.deleteAllRolesOnCase(caseData.getId());
        } else {
            List<RoleAssignment> rolesToAssign = new ArrayList<>();

            // If we have an allocated judge with an IDAM ID (added in about-to-submit step from mapping)
            Optional<Judge> allocatedJudge = judicialService.getAllocatedJudge(caseData);
            if (allocatedJudge.isPresent()
                && !isEmpty(allocatedJudge.get().getJudgeJudicialUser())
                && !isEmpty(allocatedJudge.get().getJudgeJudicialUser().getIdamId())) {

                boolean isLegalAdviser = JudgeOrMagistrateTitle.LEGAL_ADVISOR
                    .equals(allocatedJudge.get().getJudgeTitle());

                // attempt to assign allocated-[role]
                rolesToAssign.add(RoleAssignmentUtils.buildRoleAssignment(
                    caseData.getId(),
                    allocatedJudge.get().getJudgeJudicialUser().getIdamId(),
                    isLegalAdviser ? ALLOCATED_LEGAL_ADVISER.getRoleName() : ALLOCATED_JUDGE.getRoleName(),
                    isLegalAdviser ? RoleCategory.LEGAL_OPERATIONS : RoleCategory.JUDICIAL,
                    ZonedDateTime.now(),
                    null // no end date
                ));
            } else {
                log.error("Could not assign allocated-judge on case {}, no email found on the case", caseData.getId());
            }

            // get hearing judge roles to add (if any)
            rolesToAssign.addAll(judicialService.getHearingJudgeRolesForMigration(caseData));

            judicialService.migrateJudgeRoles(rolesToAssign);
        }
    }

    private void run1359(CaseDetails caseDetails) {
        migrateCaseService.doDocumentViewNCCheck(caseDetails.getId(), "DFPL-1359", caseDetails);
        caseDetails.getData().putAll(migrateCaseService.refreshDocumentViews(getCaseData(caseDetails)));
    }

    private void run1401(CaseDetails caseDetails) {
        var migrationId = "DFPL-1401";
        var possibleCaseIds = List.of(1666959378667166L);
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        caseDetails.getData().put("relatingLA", "NCC");
    }

    private void run1451(CaseDetails caseDetails) {
        var migrationId = "DFPL-1451";
        var possibleCaseIds = List.of(1669909306379829L);
        final UUID expectedOrderId = UUID.fromString("c93a824b-75ce-4ffd-ad30-ad7f42c01ed9");
        final UUID expectedHearingOrderBundleId = UUID.fromString("ebdf7ea7-a2e8-4296-be98-109b9070348e");
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        caseDetails.getData().putAll(migrateCaseService.removeDraftUploadedCMOs(getCaseData(caseDetails),
            migrationId, expectedOrderId));
        caseDetails.getData().putAll(migrateCaseService.removeHearingOrdersBundlesDrafts(getCaseData(caseDetails),
            migrationId, expectedHearingOrderBundleId));
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
    }

    private void runAM(CaseDetails caseDetails) {
        var migrationId = "DFPL-AM";

        CaseData caseData = getCaseData(caseDetails);

        Judge allocatedJudge = caseData.getAllocatedJudge();
        if (!isEmpty(allocatedJudge) && !isEmpty(allocatedJudge.getJudgeEmailAddress())) {
            Optional<String> uuid = judicialService.getJudgeUserIdFromEmail(allocatedJudge.getJudgeEmailAddress());
            // add the UUID to the allocated judge and save on the case
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
                    Optional<String> uuid = judicialService
                        .getJudgeUserIdFromEmail(val.getJudgeAndLegalAdvisor().getJudgeEmailAddress());
                    if (uuid.isPresent()) {
                        el.setValue(val.toBuilder()
                                .judgeAndLegalAdvisor(val.getJudgeAndLegalAdvisor().toBuilder()
                                    .judgeJudicialUser(JudicialUser.builder()
                                        .idamId(uuid.get())
                                        .build())
                                    .build())
                            .build());
                        return el;
                    }
                }
                return el;
            }).toList();

        if (hearings.size() > 0) {
            // don't add an empty array if there weren't any hearings beforehand
            caseDetails.getData().put("hearingDetails", modified);
        }
        caseDetails.getData().put("hasBeenAMMigrated", "Yes");
    }

    private void run1649(CaseDetails caseDetails) {
        var migrationId = "DFPL-1649";
        long expectedCaseId = 1686829053861234L;
        UUID expectedHearingId = UUID.fromString("55ecd69a-d4f3-4a1b-81ff-7144aa5f46f8");
        UUID expectedCourtBundleId = UUID.fromString("7f14382f-c16e-497e-ab8c-f3f76e212a6c");
        String messageId = "dd7e4072-41dd-46fa-a3dc-de32ee9bde93";
        CaseData caseData = getCaseData(caseDetails);

        migrateCaseService.doCaseIdCheck(caseDetails.getId(), expectedCaseId, migrationId);
        caseDetails.getData().putAll(migrateCaseService.removeJudicialMessage(caseData, migrationId, messageId));
        caseDetails.getData().putAll(migrateCaseService.removeCourtBundleByBundleId(caseData, migrationId,
            expectedHearingId, expectedCourtBundleId));
    }
}
