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
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.CourtLookUpService;
import uk.gov.hmcts.reform.fpl.service.MigrateCaseService;
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrderDocumentScopedFieldsCalculator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static java.lang.String.format;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

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

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-1226", this::run1226,
        "DFPL-1361", this::run1361,
        "DFPL-1291", this::run1291,
        "DFPL-1310", this::run1310,
        "DFPL-1371", this::run1371,
        "DFPL-1380", this::run1380,
        "DFPL-1437", this::run1437,
        "DFPL-log", this::runLog,
        "DFPL-1242", this::run1242,
        "DFPL-702", this::run702
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

    private void run1226(CaseDetails caseDetails) {
        migrateCaseService.doDocumentViewNCCheck(caseDetails.getId(), "DFPL-1226", caseDetails);
        caseDetails.getData().putAll(migrateCaseService.refreshDocumentViews(getCaseData(caseDetails)));
    }

    private void run1361(CaseDetails caseDetails) {
        var migrationId = "DFPL-1361";
        var possibleCaseIds = List.of(1680179801927341L, 1651064219316144L);
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        fieldsCalculator.calculate().forEach(caseDetails.getData()::remove);
    }

    private void run1291(CaseDetails caseDetails) {
        var migrationId = "DFPL-1291";
        var possibleCaseIds = List.of(1620403322799028L, 1627403399420113L);
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        caseDetails.getData().putAll(migrateCaseService.addCourt("165")); // Carlisle
    }

    private void run1310(CaseDetails caseDetails) {
        var migrationId = "DFPL-1310";
        Court court = getCaseData(caseDetails).getCourt();
        if (!isEmpty(court) && court.getCode().equals("150")) {
            caseDetails.getData().putAll(migrateCaseService.addCourt("554"));
        } else {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, expected court id = 150, was = %s",
                migrationId, caseDetails.getId(), isEmpty(court) ? "null" : court.getCode()
            ));
        }
    }

    private void run1371(CaseDetails caseDetails) {
        String migrationId = "DFPL-1371";
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), List.of(1667466628958196L), migrationId);
        caseDetails.getData().putAll(
            migrateCaseService.removeJudicialMessage(getCaseData(caseDetails), migrationId,
                "c6d4ed7b-ca76-47ea-87b7-9538762bab00"));
    }

    private void run1380(CaseDetails caseDetails) {
        var migrationId = "DFPL-1380";
        var possibleCaseIds = List.of(1662460879255241L);
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        caseDetails.getData().put("state", State.FINAL_HEARING);
    }

    private void run1437(CaseDetails caseDetails) {
        var migrationId = "DFPL-1437";

        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), List.of(1680258979928537L), migrationId);
        caseDetails.getData().putAll(migrateCaseService.removeFurtherEvidenceSolicitorDocuments(
            getCaseData(caseDetails), migrationId, UUID.fromString("97d51061-4ca1-4af6-94da-61160a681e2f")));
        caseDetails.getData().putAll(migrateCaseService.removeHearingFurtherEvidenceDocuments(getCaseData(caseDetails),
            migrationId,
            UUID.fromString("c696b0d4-b11b-492e-bcbb-4142d5e47258"),
            UUID.fromString("94161ba0-c229-453d-a6ce-06228aa4cf66")));
        caseDetails.getData().putAll(migrateCaseService.refreshDocumentViews(getCaseData(caseDetails)));
    }

    private void runLog(CaseDetails caseDetails) {
        log.info("Migration 'log' on case {}", caseDetails.getId());
    }
      
    @SuppressWarnings("unchecked")
    private void run1242(CaseDetails caseDetails) {
        var migrationId = "DFPL-1242";

        var invalidOrderType = "EDUCATION_SUPERVISION__ORDER";
        var validOrderType = "EDUCATION_SUPERVISION_ORDER";

        caseDetails.getData().putAll(migrateCaseService.fixOrderTypeTypo(migrationId, caseDetails));
    }


    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        final CaseData caseData = getCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        String migrationId = (String) caseDetails.getData().get(MIGRATION_ID_KEY);

        if ("DFPL-702".equals(migrationId)) {
            // update supplementary data
            String caseId = caseData.getId().toString();
            Map<String, Map<String, Map<String, Object>>> supplementaryData = new HashMap<>();
            supplementaryData.put("supplementary_data_updates",
                Map.of("$set", Map.of("HMCTSServiceId", "ABA3")));
            coreCaseDataApi.submitSupplementaryData(requestData.authorisation(),
                authToken.generate(), caseId, supplementaryData);
        }
        caseDetails.getData().remove(MIGRATION_ID_KEY);
    }

    private void run702(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        var caseId = caseData.getId();
        String caseName = caseData.getCaseName();

        String courtCode = null;
        if (caseData.getOrders() != null && StringUtils.isNotEmpty(caseData.getOrders().getCourt())) {
            courtCode = caseData.getOrders().getCourt();
        } else if (caseData.getCourt() != null) {
            courtCode = caseData.getCourt().getCode();
        }
        if (courtCode == null) {
            log.warn("Migration {id = DFPL-702, case reference = {}, case state = {}} doesn't have court info "
                    + "therefore unable to set caseManagementLocation which is mandatory in global search.",
                caseId, caseData.getState().getValue());
            return;
        }

        // migrating top level fields: case names
        Optional<Court> lookedUpCourt = courtLookUpService.getCourtByCode(courtCode);
        if (lookedUpCourt.isPresent()) {
            caseDetails.getData().put("caseManagementLocation", CaseLocation.builder()
                .baseLocation(lookedUpCourt.get().getEpimmsId())
                .region(lookedUpCourt.get().getRegionId())
                .build());

            caseDetails.getData().put("caseNameHmctsInternal", caseName);
            caseDetails.getData().put("caseManagementCategory", DynamicList.builder()
                .value(DynamicListElement.builder().code("FPL").label("Family Public Law").build())
                .listItems(List.of(
                    DynamicListElement.builder().code("FPL").label("Family Public Law").build()
                ))
                .build());
        } else {
            log.warn("Migration {id = DFPL-702, case reference = {}, case state = {}} fail to lookup ePIMMS ID "
                + "for court {}", caseId, caseData.getState().getValue(), courtCode);
        }
    }
}
