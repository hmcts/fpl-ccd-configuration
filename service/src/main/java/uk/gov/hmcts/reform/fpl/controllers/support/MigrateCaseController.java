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
import uk.gov.hmcts.reform.fpl.service.MigrateCaseService;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static java.util.UUID.fromString;
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
    private static final String POSITION_STATEMENT_LIST_CHILD = "positionStatementChildListV2";

    private final MigrateCaseService migrateCaseService;

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-1204", this::run1204,
        "DFPL-1202", this::run1202,
        "DFPL-1195", this::run1195,
        "DFPL-1218", this::run1218,
        "DFPL-1210", this::run1210,
        "DFPL-1243", this::run1243
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
        final UUID placementToRemove = fromString("88125c8b-8466-4af4-967f-197c3b82773c");
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);

        removeSpecificPlacements(caseDetails, placementToRemove);
    }

    private void run1218(CaseDetails caseDetails) {
        var migrationId = "DFPL-1218";
        var possibleCaseIds = List.of(1651753104228873L);
        final UUID placementToRemove = fromString("e32706b1-22e5-4bd9-ba05-355fe69028d0");
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);

        removeSpecificPlacements(caseDetails, placementToRemove);
    }

    private void run1243(CaseDetails caseDetails) {
        var migrationId = "DFPL-1243";
        var possibleCaseIds = List.of(1675697653441050L);

        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);

        caseDetails.getData().putAll(migrateCaseService.removePositionStatementChild(getCaseData(caseDetails),
            migrationId, fromString("ffab9b6e-436f-4c7f-afba-ee646b9fb307")));
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
