package uk.gov.hmcts.reform.fpl.controllers.support;

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
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.JudicialService;
import uk.gov.hmcts.reform.fpl.service.MigrateCaseService;
import uk.gov.hmcts.reform.fpl.service.RoleAssignmentService;
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrderDocumentScopedFieldsCalculator;
import uk.gov.hmcts.reform.fpl.utils.RoleAssignmentUtils;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole.ALLOCATED_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.LEGAL_ADVISOR;
import static uk.gov.hmcts.reform.fpl.enums.LegalAdviserRole.ALLOCATED_LEGAL_ADVISER;

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
        "DFPL-2572", this::run2572,
        "DFPL-2635", this::run2635,
        "DFPL-2636", this::run2636
    );
    private final CaseConverter caseConverter;
    private final JudicialService judicialService;

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

    private void run2572(CaseDetails caseDetails) {
        //Required to run migration for TTL
    }

    private void run2635(CaseDetails caseDetails) {
        final String migrationId = "DFPL-2635";
        final long expectedCaseId = 1721982839307738L;
        final String orgId = "F9PXZ94";
        migrateCaseService.doCaseIdCheck(caseDetails.getId(), expectedCaseId, migrationId);

        caseDetails.getData().putAll(migrateCaseService.updateOutsourcingPolicy(getCaseData(caseDetails),
            orgId, null));
    }

    private void run2636(CaseDetails caseDetails) {
        final String migrationId = "DFPL-2636";
        final long expectedCaseId = 1692795392226458L;
        final String orgId = "PHSC7LB";
        migrateCaseService.doCaseIdCheck(caseDetails.getId(), expectedCaseId, migrationId);

        caseDetails.getData().putAll(migrateCaseService.updateOutsourcingPolicy(getCaseData(caseDetails),
            orgId, null));
    }
}
