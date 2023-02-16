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
import uk.gov.hmcts.reform.fpl.enums.CaseExtensionReasonList;
import uk.gov.hmcts.reform.fpl.enums.HearingOptions;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.MigrateCaseService;
import uk.gov.hmcts.reform.fpl.service.document.DocumentListService;
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrderDocumentScopedFieldsCalculator;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static java.lang.String.format;
import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {
    private static final String MIGRATION_ID_KEY = "migrationId";

    private final MigrateCaseService migrateCaseService;
    private final DocumentListService documentListService;
    private final ManageOrderDocumentScopedFieldsCalculator fieldsCalculator;

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-1144", this::run1144,
        "DFPL-872rollback", this::run872Rollback,
        "DFPL-1072", this::run1072,
        "DFPL-1163", this::run1163,
        "DFPL-1165", this::run1165,
        "DFPL-1192", this::run1192,
        "DFPL-1215", this::run1215,
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

    private void run1144(CaseDetails caseDetails) {
        Map<String, Object> caseDetailsData = caseDetails.getData();
        caseDetailsData.put("hearingOption", HearingOptions.EDIT_PAST_HEARING);
        CaseData caseData = getCaseData(caseDetails);
        var caseId = caseData.getId();
        List<Element<Child>> childrenInCase = caseData.getAllChildren();
        LocalDate oldEightWeeksExtensionDate = caseData.getCaseCompletionDate();
        CaseExtensionReasonList oldReason = caseData.getCaseExtensionReasonList();

        if (isNotEmpty(childrenInCase) && oldReason != null) {
            log.info("Migration {id = DFPL-1144, case reference = {}} extension date migration", caseId);

            List<Element<Child>> children = childrenInCase.stream()
                .map(element -> element(element.getId(),
                    element.getValue().toBuilder()
                        .party(element.getValue().getParty().toBuilder()
                            .completionDate(oldEightWeeksExtensionDate)
                            .extensionReason(oldReason)
                            .build())
                        .build())
                ).collect(toList());

            caseDetailsData.put("children1", children);
            log.info("Migration {id = DFPL-872, case reference = {}} children extension date finish", caseId);
        } else {
            log.warn("Migration {id = DFPL-872, case reference = {}, case state = {}} doesn't have an extension ",
                caseId, caseData.getState().getValue());
        }
    }

    private void run872Rollback(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        var caseId = caseData.getId();
        List<Element<Child>> childrenInCase = caseData.getAllChildren();

        Map<String, Object> caseDetailsData = caseDetails.getData();
        if (isNotEmpty(childrenInCase)) {
            log.info("Migration {id = DFPL-872-Rollback, case reference = {}} remove child extension fields", caseId);

            List<Element<Child>> children = childrenInCase.stream()
                .map(element -> element(element.getId(),
                    element.getValue().toBuilder()
                        .party(element.getValue().getParty().toBuilder()
                            .completionDate(null)
                            .extensionReason(null)
                            .build())
                        .build())
                ).collect(toList());

            caseDetailsData.put("children1", children);
            log.info("Migration {id = DFPL-872-rollback, case reference = {}} removed child extension fields", caseId);
        } else {
            log.warn("Migration {id = DFPL-872-rollback, case reference = {}, case state = {}} doesn't have children ",
                caseId, caseData.getState().getValue());
        }
    }

    private void run1192(CaseDetails caseDetails) {
        var migrationId = "DFPL-1192";
        var expectedCaseId = 1645718564640841L;

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

    private void run1215(CaseDetails caseDetails) {
        var migrationId = "DFPL-1215";
        var expectedCaseId = 1662713946163354L;

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

    private void run1165(CaseDetails caseDetails) {
        var migrationId = "DFPL-1165";
        var expectedCaseId = 1653304601077492L;

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

    private void run1012(CaseDetails caseDetails) {
        var migrationId = "DFPL-1012";
        migrateCaseService.doCaseIdCheck(caseDetails.getId(), 1661877618161045L, migrationId);

        caseDetails.getData().putAll(migrateCaseService.removePositionStatementChild(getCaseData(caseDetails),
            migrationId, fromString("b8da3a48-441f-4210-a21c-7008d256aa32")));
    }

    private void run1064(CaseDetails caseDetails) {
        var migrationId = "DFPL-1064";
        var caseId = caseDetails.getId();
        var allowedCaseIds = List.of(1652106605168560L, 1661248269079243L, 1653561237363238L,
            1662981673264014L, 1643959297308700L, 1659605693892067L, 1658311700073897L, 1663516203585030L,
            1651066091833534L, 1657533247030897L);

        if (!allowedCaseIds.contains(caseId)) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, case id not present in allowed list",
                migrationId, caseId
            ));
        }

        caseDetails.getData().put("sendToCtsc", YesNo.NO.getValue());
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

    private void run1161(CaseDetails caseDetails) {
        var migrationId = "DFPL-1161";
        var possibleCaseIds = List.of(1660209462518487L);

        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);

        caseDetails.getData().remove("placements");
        caseDetails.getData().remove("placementsNonConfidential");
        caseDetails.getData().remove("placementsNonConfidentialNotices");
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

    private void run1072(CaseDetails caseDetails) {
        caseDetails.getData().putAll(migrateCaseService.updateIncorrectCourtCodes(getCaseData(caseDetails)));
    }

    private void run1163(CaseDetails caseDetails) {
        String migrationId = "DFPL-1163";
        migrateCaseService.doCaseIdCheck(caseDetails.getId(), 1635857454109111L, migrationId);
        caseDetails.getData().putAll(migrateCaseService.revertChildExtensionDate(getCaseData(caseDetails), migrationId,
            "309db75d-8f50-4f6e-a21a-19b903ff8f88", LocalDate.of(2022,5,9), null));
        caseDetails.getData().putAll(migrateCaseService.revertChildExtensionDate(getCaseData(caseDetails), migrationId,
            "055ed3b0-fdeb-4e83-8758-f99f387fe2c4", LocalDate.of(2022,5,9), null));
        caseDetails.getData().putAll(migrateCaseService.revertChildExtensionDate(getCaseData(caseDetails), migrationId,
            "67bd3180-3cd2-4b44-a34b-700f315ccbac", LocalDate.of(2022,5,9), null));
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
