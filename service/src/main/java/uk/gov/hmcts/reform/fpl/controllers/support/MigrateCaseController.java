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
        "SNI-8284", this::run8284,
        "DFPL-2992", this::run2992
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

    private void run8284(CaseDetails caseDetails) {
        final String migrationId = "SNI-8284";
        final List<Long> expectedCaseIds = List.of(1746789343771015L, 1746786779392316L);
        final String orgId = "BDWCNNQ";

        Long caseId = caseDetails.getId();
        migrateCaseService.doCaseIdCheckList(caseId, expectedCaseIds, migrationId);
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

            caseDetails.getData().put("standardDirectionsOrder", fixedSdo);
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
}
