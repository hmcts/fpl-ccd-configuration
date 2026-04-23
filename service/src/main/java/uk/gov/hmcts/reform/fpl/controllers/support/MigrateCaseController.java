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
import uk.gov.hmcts.reform.fpl.service.MigrateCaseService;

import java.util.List;
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

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-log", this::runLog,
        "DFPL-3080", this::run3080,
        "DFPL-3048", this::run3048,
        "DFPL-3047", this::run3047,
        "DFPL-3191", this::run3191,
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

    private void run3080(CaseDetails caseDetails) {
        final String migrationId = "DFPL-3080";
        final List<Long> expectedCaseIds = List.of(1751556200580074L, 1768391304150686L);
        final String orgId = "CPYYWBZ";

        Long caseId = caseDetails.getId();
        migrateCaseService.doCaseIdCheckList(caseId, expectedCaseIds, migrationId);
        caseDetails.getData().putAll(migrateCaseService
            .updateOutsourcingPolicy(getCaseData(caseDetails), orgId, CaseRole.EPSMANAGING.formattedName()));
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

    private void run3191(CaseDetails caseDetails) {
        final String migrationId = "DFPL-3191";
        final long expectedCaseId = 1774949045897089L;

        Long caseId = caseDetails.getId();
        migrateCaseService.doCaseIdCheck(caseId, expectedCaseId, migrationId);

        caseDetails.getData().putAll(migrateCaseService.replaceDirectionDetails(getCaseData(caseDetails),
            migrationId, "For the matter to be listed on a without notice basis"));
    }

    private void run3101(CaseDetails caseDetails) {
        final String migrationId = "DFPL-3101";
        final long expectedCaseId = 1772096689254060L;

        Long caseId = caseDetails.getId();
        migrateCaseService.doCaseIdCheck(caseId, expectedCaseId, migrationId);

        caseDetails.getData().putAll(migrateCaseService.removeFirstOther(migrationId, getCaseData(caseDetails)));
    }
}
