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
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.CourtLookUpService;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.DfjAreaLookUpService;
import uk.gov.hmcts.reform.fpl.service.JudicialService;
import uk.gov.hmcts.reform.fpl.service.MigrateCaseService;
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrderDocumentScopedFieldsCalculator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static java.lang.String.format;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole.ALLOCATED_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole.HEARING_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.LegalAdviserRole.ALLOCATED_LEGAL_ADVISER;
import static uk.gov.hmcts.reform.fpl.enums.LegalAdviserRole.HEARING_LEGAL_ADVISER;
import static uk.gov.hmcts.reform.fpl.service.CourtLookUpService.RCJ_HIGH_COURT_CODE;

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
    private final JudicialService judicialService;

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-1359", this::run1359,
        "DFPL-1401", this::run1401,
        "DFPL-1451", this::run1451,
        "DFPL-1466", this::run1466,
        "DFPL-1501", this::run1616,
        "DFPL-1584", this::run1612,
        "DFPL-1352", this::run1352,
        "DFPL-1486", this::run1486
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

        // Allocated judge
        Optional<Judge> allocatedJudge = judicialService.getAllocatedJudge(caseData);
        if (allocatedJudge.isPresent() && !isEmpty(allocatedJudge.get().getJudgeEmailAddress())) {
            Optional<String> id = judicialService.getJudgeUserIdFromEmail(allocatedJudge.get().getJudgeEmailAddress());
            if (id.isPresent()) {
                if (allocatedJudge.get().getJudgeTitle().equals(JudgeOrMagistrateTitle.LEGAL_ADVISOR)) {
                    // attempt to assign legal adviser
                    judicialService.assignLegalAdviserCaseRole(caseData.getId(), id.get(),
                        ALLOCATED_LEGAL_ADVISER.getRoleName());
                } else {
                    // attempt to assign judge
                    judicialService.assignJudgeCaseRole(caseData.getId(), id.get(), ALLOCATED_JUDGE.getRoleName());
                }
            } else {
                log.error("Could not assign allocated-judge on case {}, could not fetch userId from IDAM",
                    caseData.getId());
            }
        } else {
            log.error("Could not assign allocated-judge on case {}, no email found on the case", caseData.getId());
        }

        Set<JudgeAndLegalAdvisor> hearingJudges = judicialService.getHearingJudges(caseData);
        hearingJudges.forEach(judge -> {
            Optional<String> id = judicialService.getJudgeUserIdFromEmail(judge.getJudgeEmailAddress());
            if (id.isPresent()) {
                if (judge.getJudgeTitle().equals(JudgeOrMagistrateTitle.LEGAL_ADVISOR)) {
                    // attempt to assign legal adviser
                    judicialService.assignLegalAdviserCaseRole(caseData.getId(), id.get(),
                        HEARING_LEGAL_ADVISER.getRoleName());
                } else {
                    // attempt to assign judge
                    judicialService.assignJudgeCaseRole(caseData.getId(), id.get(), HEARING_JUDGE.getRoleName());
                }

            } else {
                log.error("Could not assign hearing-judge on case {}, could not fetch userId from IDAM",
                    caseData.getId());
            }
        });
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

    private void run1466(CaseDetails caseDetails) {
        var migrationId = "DFPL-1466";
        var possibleCaseIds = List.of(1665396049325141L);
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);

        caseDetails.getData().putAll(migrateCaseService.removePositionStatementChild(getCaseData(caseDetails),
            migrationId, UUID.fromString("b8da3a48-441f-4210-a21c-7008d256aa32")));
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

    private void run1352(CaseDetails caseDetails) {
        var migrationId = "DFPL-1352";

        CaseData caseData = getCaseData(caseDetails);

        if (caseData.getCourt().getCode().equals(RCJ_HIGH_COURT_CODE)) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, Skipping migration as case is in the High Court",
                migrationId, caseData.getId()
            ));
        }
        if (caseData.getSendToCtsc().equals("Yes")) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, Skipping migration as case is already sending to the CTSC",
                migrationId, caseData.getId()
            ));
        }
        caseDetails.getData().put("sendToCtsc", "Yes");
    }

    private void run1486(CaseDetails caseDetails) {
        var migrationId = "DFPL-1486";
        caseDetails.getData().putAll(migrateCaseService.addRelatingLA(migrationId, caseDetails.getId()));
    }
}
