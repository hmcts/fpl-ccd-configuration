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
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.MigrateCaseService;
import uk.gov.hmcts.reform.fpl.service.document.DocumentListService;
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrderDocumentScopedFieldsCalculator;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Consumer;

import static java.lang.String.format;
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
    private final DocumentListService documentListService;
    private final ManageOrderDocumentScopedFieldsCalculator fieldsCalculator;

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-1204", this::run1204,
        "DFPL-1202", this::run1202,
        "DFPL-1195", this::run1195,
        "DFPL-1065", this::run1065,
        "DFPL-1029", this::run1029,
        "DFPL-1162", this::run1162,
        "DFPL-1156", this::run1156,
        "DFPL-1218", this::run1218
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

    private void run1029(CaseDetails caseDetails) {
        var migrationId = "DFPL-1029";
        var expectedCaseId = 1638876373455956L;
        CaseData caseData = getCaseData(caseDetails);

        Long caseId = caseData.getId();
        if (caseId != expectedCaseId) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, expected case id %d",
                migrationId, caseId, expectedCaseId
            ));
        }
        fieldsCalculator.calculate().forEach(caseDetails.getData()::remove);
    }

    private void run1065(CaseDetails caseDetails) {
        var migrationId = "DFPL-1065";
        var caseId = caseDetails.getId();
        var allowedCaseIds = List.of(1668006391899836L, 1669021882046010L, 1664550708978381L,
            1666192242451225L, 1669386919276017L, 1667557188169842L, 1669305338431433L, 1667208965579320L,
            1667557388743867L, 1638284933401539L, 1667558394262009L, 1669035467933533L, 1666272019920949L,
            1665658257668862L, 1664903454848680L, 1666178550940636L, 1666022942850998L);

        if (!allowedCaseIds.contains(caseId)) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, case id not present in allowed list",
                migrationId, caseId
            ));
        }

        caseDetails.getData().put("sendToCtsc", YesNo.YES.getValue());
    }

    private void run1156(CaseDetails caseDetails) {
        var migrationId = "DFPL-1156";
        migrateCaseService.doCaseIdCheck(caseDetails.getId(),1668086461879587L, migrationId);

        caseDetails.getData().putAll(migrateCaseService.removeApplicationDocument(getCaseData(caseDetails),
            migrationId, UUID.fromString("1862581c-b628-4fc8-afb8-8576d3def0f1")));
        CaseDetails details = CaseDetails.builder().data(caseDetails.getData()).build();
        caseDetails.getData().putAll(documentListService.getDocumentView(getCaseData(details)));
    }

    private void run1162(CaseDetails caseDetails) {
        var migrationId = "DFPL-1162";
        migrateCaseService.doCaseIdCheck(caseDetails.getId(), 1673628190034209L, migrationId);
        migrateCaseService.verifyGatekeepingOrderUrgentHearingOrderExistWithGivenFileName(getCaseData(caseDetails),
            migrationId, "PO23C50013 HCC V Carter EPO with remote hearing directions march 2021.pdf");

        caseDetails.getData().remove("urgentHearingOrder");
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
}
