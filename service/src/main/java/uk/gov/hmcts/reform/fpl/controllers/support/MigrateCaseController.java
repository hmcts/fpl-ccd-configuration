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
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.JudicialService;
import uk.gov.hmcts.reform.fpl.service.MigrateCaseService;
import uk.gov.hmcts.reform.fpl.service.RoleAssignmentService;
import uk.gov.hmcts.reform.fpl.service.SendNewMessageJudgeService;
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrderDocumentScopedFieldsCalculator;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Consumer;

import static uk.gov.hmcts.reform.fpl.controllers.ReturnApplicationController.DATE_SUBMITTED;
import static uk.gov.hmcts.reform.fpl.controllers.ReturnApplicationController.LAST_SUBMITTED_DATE;

@Slf4j
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MigrateCaseController extends CallbackController {
    public static final String MIGRATION_ID_KEY = "migrationId";
    private final MigrateCaseService migrateCaseService;
    private final ManageOrderDocumentScopedFieldsCalculator fieldsCalculator;
    private final RoleAssignmentService roleAssignmentService;

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-log", this::runLog,
        "DFPL-2992", this::run2992,
        "DFPL-2677", this::run2677,
        "DFPL-2677-rollback", this::rollback2677,
        "DFPL-3015", this::run3015,
        "DFPL-3028", this::run3028,
        "DFPL-3033", this::run3033
    );
    private final CaseConverter caseConverter;
    private final JudicialService judicialService;
    private final SendNewMessageJudgeService sendNewMessageJudgeService;

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

    private void run3028(CaseDetails caseDetails) {
        final String migrationId = "DFPL-3028";
        final long expectedCaseId = 1752740940481434L;
        final String orgId = "GKVZ178";

        Long caseId = caseDetails.getId();
        migrateCaseService.doCaseIdCheck(caseId, expectedCaseId, migrationId);
        caseDetails.getData().putAll(migrateCaseService.updateOutsourcingPolicy(getCaseData(caseDetails), orgId, null));
    }

    private void run2992(CaseDetails caseDetails) {
        final String migrationId = "DFPL-2992";
        final List<Long> expectedCaseIds = List.of(1763039644207964L);

        Long caseId = caseDetails.getId();
        migrateCaseService.doCaseIdCheckList(caseId, expectedCaseIds, migrationId);
        CaseData caseData = getCaseData(caseDetails);

        if (caseDetails.getId().equals(expectedCaseIds.getFirst())) {
            StandardDirectionOrder standardDirectionOrder = caseData.getStandardDirectionOrder();

            String judgeEmail = migrateCaseService.fixInvalidEmailAddressFormat(
                standardDirectionOrder.getJudgeAndLegalAdvisor().getJudgeEmailAddress());

            StandardDirectionOrder fixedSdo = standardDirectionOrder.toBuilder()
                .judgeAndLegalAdvisor(standardDirectionOrder.getJudgeAndLegalAdvisor().toBuilder()
                    .judgeEmailAddress(judgeEmail)
                    .build())
                .build();

            caseDetails.getData().put("standardDirectionOrder", fixedSdo);
            return;
        }

        // Leaving this option in here for any future incidents with email issues
        if (caseDetails.getId().equals(1744715537303275L)) {
            UUID expectedCancelledHearingId = UUID.fromString("43d52bcc-1d58-49bb-be0d-8d920d9eee91");
            List<Element<HearingBooking>> cancelledHearings = caseData.getCancelledHearingDetails();

            caseDetails.getData().remove("tempAllocatedJudge");
            caseDetails.getData().put("cancelledHearingDetails",
                migrateCaseService.replaceHearingJudgeEmailAddress(migrationId, cancelledHearings,
                    expectedCancelledHearingId, caseId));
        }
    }

    private void run2677(CaseDetails caseDetails) {
        if (caseDetails.getData().get(DATE_SUBMITTED) == null
            || caseDetails.getData().get(LAST_SUBMITTED_DATE) != null) {
            throw new AssertionError("[Case %s], dateSubmitted is null or lastSubmittedDate is not null"
                .formatted(caseDetails.getId()));
        }
        caseDetails.getData().put(LAST_SUBMITTED_DATE, caseDetails.getData().get(DATE_SUBMITTED));
        caseDetails.getData().put(DATE_SUBMITTED, null);
    }

    private void rollback2677(CaseDetails caseDetails) {
        if (caseDetails.getData().get(LAST_SUBMITTED_DATE) == null
            || caseDetails.getData().get(DATE_SUBMITTED) != null) {
            throw new AssertionError("[Case %s], lastSubmittedDate is null or dateSubmitted is not null"
                .formatted(caseDetails.getId()));
        }
        caseDetails.getData().put(DATE_SUBMITTED, caseDetails.getData().get(LAST_SUBMITTED_DATE));
        caseDetails.getData().remove(LAST_SUBMITTED_DATE);
    }

    private void run3015(CaseDetails caseDetails) {
        final String migrationId = "DFPL-3015";
        final Long expectedCaseId = 1765793464370132L;
        final String orgId = "ZBGD22I";

        Long caseId = caseDetails.getId();
        migrateCaseService.doCaseIdCheck(caseId, expectedCaseId, migrationId);
        caseDetails.getData().putAll(migrateCaseService
            .updateRespondentPolicy(getCaseData(caseDetails), orgId, null, 0));
    }

    private void run3033(CaseDetails caseDetails) {
        final String migrationId = "DFPL-3033";
        final Long expectedCaseId = 1699540495365743L;
        final String orgId = "CS35UMJ";

        Long caseId = caseDetails.getId();
        migrateCaseService.doCaseIdCheck(caseId, expectedCaseId, migrationId);
        caseDetails.getData().putAll(migrateCaseService
            .updateOutsourcingPolicy(getCaseData(caseDetails), orgId, CaseRole.EPSMANAGING.formattedName()));
    }
}
