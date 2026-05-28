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
import uk.gov.hmcts.reform.fpl.model.ConfidentialRefusedOrders;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.MigrateCaseService;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Slf4j
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MigrateCaseController extends CallbackController {
    public static final String MIGRATION_ID_KEY = "migrationId";
    private final MigrateCaseService migrateCaseService;

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-log", this::runLog,
        "DFPL-2773", this::run2773,
        "DFPL-2773-rollback", this::run2773Rollback,
        "DFPL-3227", this::run3227,
        "DFPL-3048", this::run3048,
        "DFPL-3047", this::run3047,
        "DFPL-3101", this::run3101
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

    private void run3227(CaseDetails caseDetails) {
        final String migrationId = "DFPL-3227";
        final long expectedCaseId = 1777547979393690L;
        final CaseData caseData = getCaseData(caseDetails);
        final String replacementEmail = caseData.getAllocatedJudge().getJudgeEmailAddress();
        final JudgeAndLegalAdvisor replacedJudge = caseData.getStandardDirectionOrder().getJudgeAndLegalAdvisor()
            .toBuilder()
            .judgeEmailAddress(replacementEmail)
            .build();
        Long caseId = caseDetails.getId();
        migrateCaseService.doCaseIdCheck(caseId, expectedCaseId, migrationId);

        caseDetails.getData().put("standardDirectionOrder", caseData.getStandardDirectionOrder().toBuilder()
            .judgeAndLegalAdvisor(replacedJudge)
            .build());
    }

    private void run3048(CaseDetails caseDetails) {
        final String migrationId = "DFPL-3048";
        final Long expectedCaseId = 1769766848334996L;

        Long caseId = caseDetails.getId();
        final CaseData caseData = getCaseData(caseDetails);

        migrateCaseService.doCaseIdCheck(caseId, expectedCaseId, migrationId);
        caseDetails.getData().put("hearing",
            caseData.getHearing().toBuilder().hearingUrgencyDetails("***").build());
    }

    private void run3047(CaseDetails caseDetails) {
        final String migrationId = "DFPL-3047";
        final Long expectedCaseId = 1757072393794849L;
        final String orgId = "CVPRECR";

        Long caseId = caseDetails.getId();
        migrateCaseService.doCaseIdCheck(caseId, expectedCaseId, migrationId);
        caseDetails.getData().putAll(migrateCaseService
            .updateRespondentPolicy(getCaseData(caseDetails), orgId, null, 0));
    }

    private void run3101(CaseDetails caseDetails) {
        final String migrationId = "DFPL-3101";
        final long expectedCaseId = 1772096689254060L;

        Long caseId = caseDetails.getId();
        migrateCaseService.doCaseIdCheck(caseId, expectedCaseId, migrationId);

        caseDetails.getData().putAll(migrateCaseService.removeFirstOther(migrationId, getCaseData(caseDetails)));
    }

    private void run2773(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if (isNotEmpty(caseData.getRefusedHearingOrders())) {
            caseDetails.getData().put("refusedHearingOrders", migrateRefusedOrders(caseData.getRefusedHearingOrders()));
        }

        // Process all confidential refused orders
        ConfidentialRefusedOrders existingConfidentialRefusedOrders = caseData.getConfidentialRefusedOrders();
        if (existingConfidentialRefusedOrders != null) {
            existingConfidentialRefusedOrders.processAllConfidentialOrders((suffix, refusedOrderElements) -> {
                if (isNotEmpty(refusedOrderElements)) {
                    caseDetails.getData().put(
                        existingConfidentialRefusedOrders.getFieldBaseName() + suffix,
                        migrateRefusedOrders(refusedOrderElements));
                }
            });
        }
    }

    // one off migration only, can't see any reason to keep this method in the future
    private List<Element<HearingOrder>> migrateRefusedOrders(List<Element<HearingOrder>> refusedOrders) {
        return refusedOrders.stream()
            .map(refusedOrderElement -> element(
                refusedOrderElement.getId(),
                refusedOrderElement.getValue().toBuilder()
                    .refusedOrder(refusedOrderElement.getValue().getOrder())
                    .order(null)
                    .build()))
            .toList();
    }

    private void run2773Rollback(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if (isNotEmpty(caseData.getRefusedHearingOrders())) {
            caseDetails.getData().put("refusedHearingOrders",
                rollbackRefusedOrders(caseData.getRefusedHearingOrders()));
        }

        // Process all confidential refused orders
        ConfidentialRefusedOrders existingConfidentialRefusedOrders = caseData.getConfidentialRefusedOrders();
        if (existingConfidentialRefusedOrders != null) {
            existingConfidentialRefusedOrders.processAllConfidentialOrders((suffix, refusedOrderElements) -> {
                if (isNotEmpty(refusedOrderElements)) {
                    caseDetails.getData().put(
                        existingConfidentialRefusedOrders.getFieldBaseName() + suffix,
                        rollbackRefusedOrders(refusedOrderElements));
                }
            });
        }
    }

    // one off migration only, can't see any reason to keep this method in the future
    private List<Element<HearingOrder>> rollbackRefusedOrders(List<Element<HearingOrder>> refusedOrders) {
        return refusedOrders.stream()
            .map(refusedOrderElement -> element(
                refusedOrderElement.getId(),
                refusedOrderElement.getValue().toBuilder()
                    .refusedOrder(null)
                    .order(refusedOrderElement.getValue().getRefusedOrder())
                    .build()))
            .toList();
    }
}
