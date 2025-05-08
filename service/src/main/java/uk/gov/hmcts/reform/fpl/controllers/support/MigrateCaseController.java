package uk.gov.hmcts.reform.fpl.controllers.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.am.model.RoleAssignment;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.CallbackController;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.noc.ChangeOfRepresentation;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.JudicialService;
import uk.gov.hmcts.reform.fpl.service.MigrateCaseService;
import uk.gov.hmcts.reform.fpl.service.RoleAssignmentService;
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
        "DFPL-2360", this::run2360,
        "DFPL-2572", this::run2572,
        "DFPL-2421", this::run2421,
        "DFPL-2421-rollback", this::rollback2421,
        "DFPL-2487", this::run2487,
        "DFPL-2740", this::run2740,
        "DFPL-2744", this::run2744,
        "DFPL-2739", this::run2739,
        "DFPL-2733", this::run2733
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

    private void run2733(CaseDetails caseDetails) {
        final String migrationId = "DFPL-2733";
        final long expectedCaseId = 1718621798109264L;
        final String orgId = "NTJRIVB";

        migrateCaseService.doCaseIdCheck(caseDetails.getId(), expectedCaseId, migrationId);

        caseDetails.getData().putAll(migrateCaseService.updateOutsourcingPolicy(getCaseData(caseDetails),
            orgId, null));
    }

    private void run2360(CaseDetails caseDetails) {
        final String migrationId = "DFPL-2360";
        // all existing cases need this field now, new cases will be populated in case initiation
        caseDetails.getData().put("hasRespondentLA", YesNo.NO);
    }

    private void run2421(CaseDetails caseDetails) {
        final String migrationId = "DFPL-2421";
        migrateCaseService.migrateOthersToOthersV2(getCaseData(caseDetails), caseDetails.getData(), migrationId);
    }

    private void rollback2421(CaseDetails caseDetails) {
        final String migrationId = "DFPL-2421-rollback";
        migrateCaseService.rollbackOthersV2ToOthers(getCaseData(caseDetails), caseDetails.getData(), migrationId);
    }

    private void run2487(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        judicialService.cleanupHearingRoles(caseData.getId());

        List<RoleAssignment> rolesToAssign = judicialService.getHearingJudgeRolesForMigration(caseData);

        log.info("Attempting to create {} roles on case {}", rolesToAssign.size(), caseData.getId());
        judicialService.migrateJudgeRoles(rolesToAssign);
    }

    private void run2740(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        migrateCaseService.doCaseIdCheck(caseDetails.getId(), 1743167066103323L, "DFPL-2740");

        List<Element<ChangeOfRepresentation>> changes = caseData.getChangeOfRepresentatives();
        List<Element<ChangeOfRepresentation>> after = changes.stream().map(element -> {
            ChangeOfRepresentation value = element.getValue();
            if (element.getId().equals(UUID.fromString("625f113c-5673-4b35-bbf1-6507fcf9ec43"))) {
                element.setValue(value.toBuilder()
                    .child(value.getChild().substring(0, 5))
                    .build());
            }
            return element;
        }).toList();

        caseDetails.getData().put("changeOfRepresentatives", after);
        caseDetails.getData().remove("noticeOfProceedingsBundle");
    }

    private void run2744(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        migrateCaseService.doCaseIdCheck(caseDetails.getId(), 1743174504422687L, "DFPL-2744");

        if (caseData.getHearingOrdersBundlesDrafts().size() == 1
            && caseData.getHearingOrdersBundlesDrafts().get(0).getId()
                .equals(UUID.fromString("cff80b00-7300-4cd5-b0cb-f9f7a2ecd862"))) {
            caseDetails.getData().remove("hearingOrdersBundlesDrafts");
        } else {
            throw new AssertionError("Different numbers of hearingOrdersBundlesDrafts or different UUID");
        }
    }

    private void run2739(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        migrateCaseService.doCaseIdCheck(caseDetails.getId(), 1726944362364630L, "DFPL-2739");

        caseDetails.getData().putAll(migrateCaseService.removeDraftOrderFromAdditionalApplication(caseData,
            "DFPL-2739",
            UUID.fromString("3ef67b37-17ee-48ca-9d32-58c887a6918d"),
            UUID.fromString("dbe742bb-f7a1-4373-8100-52261c81ef34")));
    }
}
