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
import uk.gov.hmcts.reform.fpl.enums.HearingOptions;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.CourtLookUpService;
import uk.gov.hmcts.reform.fpl.service.MigrateCaseService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;

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
    private static final String PLACEMENT = "placements";
    private static final String PLACEMENT_NON_CONFIDENTIAL = "placementsNonConfidential";
    private static final String PLACEMENT_NON_CONFIDENTIAL_NOTICES = "placementsNonConfidentialNotices";

    private final MigrateCaseService migrateCaseService;

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-1204", this::run1204,
        "DFPL-1202", this::run1202,
        "DFPL-1195", this::run1195,
        "DFPL-1218", this::run1218,
        "DFPL-1210", this::run1210,
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

        return respond(caseDetails);
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

    private void run1202(CaseDetails caseDetails) {
        var migrationId = "DFPL-1202";
        var possibleCaseIds = List.of(1649150882331141L);

        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);

        caseDetails.getData().remove(PLACEMENT);
        caseDetails.getData().remove(PLACEMENT_NON_CONFIDENTIAL);
        caseDetails.getData().remove(PLACEMENT_NON_CONFIDENTIAL_NOTICES);
    }

    private void run1195(CaseDetails caseDetails) {
        var migrationId = "DFPL-1195";
        var possibleCaseIds = List.of(1655911528192218L);

        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);

        caseDetails.getData().remove(PLACEMENT);
        caseDetails.getData().remove(PLACEMENT_NON_CONFIDENTIAL);
        caseDetails.getData().remove(PLACEMENT_NON_CONFIDENTIAL_NOTICES);
    }

    private void run1204(CaseDetails caseDetails) {
        var migrationId = "DFPL-1204";
        var possibleCaseIds = List.of(1638528543085011L);
        final UUID placementToRemove = UUID.fromString("88125c8b-8466-4af4-967f-197c3b82773c");
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);

        removeSpecificPlacements(caseDetails, placementToRemove);
    }

    private void run1218(CaseDetails caseDetails) {
        var migrationId = "DFPL-1218";
        var possibleCaseIds = List.of(1651753104228873L);
        final UUID placementToRemove = UUID.fromString("e32706b1-22e5-4bd9-ba05-355fe69028d0");
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);

        removeSpecificPlacements(caseDetails, placementToRemove);
    }

    private void removeSpecificPlacements(CaseDetails caseDetails,UUID placementToRemove) {
        CaseData caseData = getCaseData(caseDetails);

        List<Element<Placement>> placementsToKeep = caseData.getPlacementEventData().getPlacements().stream()
            .filter(x -> !x.getId().equals(placementToRemove)).collect(toList());
        caseData.getPlacementEventData().setPlacements(placementsToKeep);

        List<Element<Placement>> nonConfidentialPlacementsToKeep = caseData.getPlacementEventData()
            .getPlacementsNonConfidential(false);

        List<Element<Placement>> nonConfidentialNoticesPlacementsToKeep = caseData.getPlacementEventData()
            .getPlacementsNonConfidential(true);

        caseDetails.getData().put(PLACEMENT, placementsToKeep);
        caseDetails.getData().put(PLACEMENT_NON_CONFIDENTIAL, nonConfidentialPlacementsToKeep);
        caseDetails.getData().put(PLACEMENT_NON_CONFIDENTIAL_NOTICES, nonConfidentialNoticesPlacementsToKeep);
    }
    
    private void run1210(CaseDetails caseDetails) {
        String migrationId = "DFPL-1210";
        Map<String, Object> caseDetailsData = caseDetails.getData();
        migrateCaseService.doCaseIdCheck(caseDetails.getId(), 1615556003529811L, migrationId);
        migrateCaseService.doHearingOptionCheck(caseDetails.getId(),
            Optional.of((String) caseDetails.getData().get("hearingOption")).orElse(""),
            "EDIT_HEARING", migrationId);
        caseDetailsData.put("hearingOption", HearingOptions.EDIT_PAST_HEARING);
    }
}
