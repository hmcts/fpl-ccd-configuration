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
import uk.gov.hmcts.reform.fpl.service.MigrateCaseService;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Consumer;

@Api
@Slf4j
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MigrateCaseController extends CallbackController {
    public static final String MIGRATION_ID_KEY = "migrationId";

    private final MigrateCaseService migrateCaseService;

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-1921", this::run1921
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

    private void run1887(CaseDetails caseDetails) {
        var migrationId = "DFPL-1887";
        var possibleCaseIds = List.of(1684922324530563L);
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);

        String orgId = "BDWCNNQ";

        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().putAll(migrateCaseService.changeThirdPartyStandaloneApplicant(caseData, orgId));
        caseDetails.getData().putAll(migrateCaseService.removeApplicantEmailAndStopNotifyingTheirColleagues(caseData,
            migrationId, "f2ee2c01-7cab-4ff0-aa28-fd980a7da15a"));
    }

    private void run1810(CaseDetails caseDetails) {
        var migrationId = "DFPL-1810";
        var possibleCaseIds = List.of(1652188944970682L);
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);

        caseDetails.getData().putAll(migrateCaseService.removeSkeletonArgument(getCaseData(caseDetails),
            "fb4f5a39-b0af-44a9-9eb2-c7dd4cf06fa5", migrationId));
    }

    private void run1802(CaseDetails caseDetails) {
        var migrationId = "DFPL-1802";
        var possibleCaseIds = List.of(1683295453455055L);
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);

        CaseData caseData = getCaseData(caseDetails);
        caseDetails.getData().putAll(migrateCaseService.removeElementFromLocalAuthorities(caseData, migrationId,
            UUID.fromString("d44b1079-9f55-48be-be6e-757b5e600f04")));
    }

    private void run1837(CaseDetails caseDetails) {
        var migrationId = "DFPL-1837";
        var possibleCaseIds = List.of(1649154482198017L);
        var expectedHearingId = UUID.fromString("6aa300bc-97b4-4c15-ac2c-6804f4fef3cb");
        var expectedDocId = UUID.fromString("982dc7f7-11a7-4eb6-b1ab-7778d20dcf27");
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);

        CaseData caseData = getCaseData(caseDetails);
        caseDetails.getData().putAll(migrateCaseService.removeHearingFurtherEvidenceDocuments(caseData,
            migrationId, expectedHearingId, expectedDocId));
    }

    private void run1899(CaseDetails caseDetails) {
        var migrationId = "DFPL-1899";
        var possibleCaseIds = List.of(1698314232873794L);
        var thresholdDetailsStartIndex = 348;
        var thresholdDetailsEndIndex = 452;

        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        CaseData caseData = getCaseData(caseDetails);
        caseDetails.getData().putAll(migrateCaseService.removeCharactersFromThresholdDetails(caseData,
            migrationId, thresholdDetailsStartIndex, thresholdDetailsEndIndex));
    }

    private void run1905(CaseDetails caseDetails) {
        migrateCaseService.clearChangeOrganisationRequest(caseDetails);
    }

    private void run1921(CaseDetails caseDetails) {
        var migrationId = "DFPL-1921";
        var possibleCaseIds = List.of(1689599455058930L);

        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        CaseData caseData = getCaseData(caseDetails);
        caseDetails.getData().putAll(migrateCaseService.removeCaseSummaryByHearingId(caseData, migrationId,
            UUID.fromString("37ab2651-b3f6-40e2-b880-275a6dba51cd")));
    }

    private void run1915(CaseDetails caseDetails) {
        var migrationId = "DFPL-1915";
        var possibleCaseIds = List.of(1671617151971048L);
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);

        CaseData caseData = getCaseData(caseDetails);
        caseDetails.getData().putAll(migrateCaseService.removeJudicialMessage(caseData, migrationId,
            "03aa4e2e-03dc-48f6-9c0c-8b2136b68c6f"));

        caseDetails.getData().putAll(migrateCaseService.removeClosedJudicialMessage(caseData, migrationId,
            "c5da68b1-f67b-4442-8bfe-227b7b21f02e"));
    }

    private void run1898(CaseDetails caseDetails) {
        var migrationId = "DFPL-1898";
        var possibleCaseIds = List.of(1698163422633306L);
        var noticeOfProceedingsBundleId = UUID.fromString("b2bd4359-91a2-44fc-a7a5-ba8b8da27a25");

        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        CaseData caseData = getCaseData(caseDetails);
        caseDetails.getData().putAll(migrateCaseService.removeNoticeOfProceedingsBundle(caseData,
            String.valueOf(noticeOfProceedingsBundleId), migrationId));
    }
}
