package uk.gov.hmcts.reform.fpl.controllers.support;

import io.swagger.annotations.Api;
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
import uk.gov.hmcts.reform.fpl.service.MigrateCFVService;
import uk.gov.hmcts.reform.fpl.service.MigrateCaseService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY;

@Api
@Slf4j
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MigrateCaseController extends CallbackController {
    public static final String MIGRATION_ID_KEY = "migrationId";

    private final MigrateCaseService migrateCaseService;
    private final MigrateCFVService migrateCFVService;

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-log", this::runLogMigration,
        "DFPL-1855", this::run1855,
        "DFPL-1954", this::run1954,
        "DFPL-1948", this::run1948,
        "DFPL-1991", this::run1991
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

    private void run1855(CaseDetails caseDetails) {
        var migrationId = "DFPL-1855";
        caseDetails.getData().putAll(migrateCaseService.fixIncorrectCaseManagementLocation(caseDetails, migrationId));
    }

    private void runLogMigration(CaseDetails caseDetails) {
        log.info("Dummy migration for case {}", caseDetails.getId());
    }

    private void run1954(CaseDetails caseDetails) {
        var migrationId = "DFPL-1954";
        var possibleCaseIds = List.of(1680510780369230L);
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);

        String orgId = "2Z69Q0U";

        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().putAll(migrateCaseService.changeThirdPartyStandaloneApplicant(caseData, orgId));
        caseDetails.getData().putAll(migrateCaseService.removeApplicantEmailAndStopNotifyingTheirColleagues(caseData,
            migrationId, "3cb2d4b1-d0cb-46d7-99e1-913cb15bfa0e"));
    }

    private void run1948(CaseDetails caseDetails) {
        var migrationId = "DFPL-1948";
        var possibleCaseIds = List.of(1681814563345287L);
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);

        String orgId = "P71FQC0";

        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().putAll(migrateCaseService.changeThirdPartyStandaloneApplicant(caseData, orgId));
        caseDetails.getData().putAll(migrateCaseService.removeApplicantEmailAndStopNotifyingTheirColleagues(caseData,
            migrationId, "d7beca42-edbd-42db-a922-bcbec58b8306"));
    }

    public void run1991(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        caseDetails.getData().putAll(migrateCFVService.migrateMissingApplicationDocuments(caseData,
            DESIGNATED_LOCAL_AUTHORITY, List.of(LASOLICITOR)));
    }
}
