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
import uk.gov.hmcts.reform.fpl.enums.HearingOptions;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.DfjAreaLookUpService;
import uk.gov.hmcts.reform.fpl.service.MigrateCaseService;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {
    private static final String MIGRATION_ID_KEY = "migrationId";
    private static final String PLACEMENT = "placements";
    private static final String PLACEMENT_NON_CONFIDENTIAL = "placementsNonConfidential";
    private static final String PLACEMENT_NON_CONFIDENTIAL_NOTICES = "placementsNonConfidentialNotices";

    private final MigrateCaseService migrateCaseService;
    private final DfjAreaLookUpService dfjAreaLookUpService;

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-1124", this::run1124,
        "DFPL-1124Rollback", this::run1124Rollback,
        "DFPL-1204", this::run1204,
        "DFPL-1202", this::run1202,
        "DFPL-1195", this::run1195,
        "DFPL-1218", this::run1218,
        "DFPL-1210", this::run1210
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

    private void run1124(CaseDetails caseDetails) {
        log.info("Migrating case {}", caseDetails.getId());
    }


    private void run1124Rollback(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        var caseId = caseData.getId();
        if (Objects.nonNull(caseData.getDfjArea())) {
            caseDetails.getData().remove("dfjArea");
            caseDetails.getData().keySet().removeAll(dfjAreaLookUpService.getAllCourtFields());
            log.info("Rollback {id = DFPL-1124, case reference = {}} removed dfj area and relevant court field",
                caseId);
        } else {
            log.warn("Rollback {id = DFPL-1124, case reference = {}} doesn't have dfj area and relevant court field",
                caseId);
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
