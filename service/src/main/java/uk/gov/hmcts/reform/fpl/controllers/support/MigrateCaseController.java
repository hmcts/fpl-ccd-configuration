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
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseNote;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.MigrateCaseService;
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrderDocumentScopedFieldsCalculator;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Consumer;

import static java.lang.String.format;
import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.State.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {
    private static final String MIGRATION_ID_KEY = "migrationId";

    private final MigrateCaseService migrateCaseService;

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-985", this::run985,
        "DFPL-1012", this::run1012,
        "DFPL-1064", this::run1064,
        "DFPL-872", this::run872,
        "DFPL-979", this::run979,
        "DFPL-1006", this::run1006,
        "DFPL-1065", this::run1065,
        "DFPL-872rollback", this::run872Rollback,
        "DFPL-1029", this::run1029 // DON'T DELETE THIS MIGRATION, POTENTIALLY ONGOING ISSUES
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

    private void run872(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        var caseId = caseData.getId();
        List<Element<Child>> childrenInCase = caseData.getAllChildren();
        LocalDate oldEightWeeksExtensionDate = caseData.getCaseCompletionDate();
        CaseExtensionReasonList oldReason = caseData.getCaseExtensionReasonList();
        Map<String, Object> caseDetailsData = caseDetails.getData();

        if (isNotEmpty(childrenInCase) && oldReason != null) {
            log.info("Migration {id = DFPL-872, case reference = {}} extension date migration", caseId);

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

    private void run979(CaseDetails caseDetails) {
        var migrationId = "DFPL-979";
        var expectedCaseId = 1648556593632182L;
        var expectedCaseNotesId = List.of("c0c0c620-055e-488c-a6a9-d5e7ec35c210",
            "0a202483-b7e6-44a1-a28b-8c9342f67967");
        removeCaseNote(caseDetails, migrationId, expectedCaseId, expectedCaseNotesId);
    }

    private void removeCaseNote(CaseDetails caseDetails, String migrationId, long expectedCaseId,
                                List<String> expectedCaseNotesId) {
        CaseData caseData = getCaseData(caseDetails);
        Long caseId = caseData.getId();

        if (caseId != expectedCaseId) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, expected case id %d",
                migrationId, caseId, expectedCaseId
            ));
        }

        List<UUID> caseNotesId = expectedCaseNotesId.stream().map(UUID::fromString).collect(toList());

        List<Element<CaseNote>> resultCaseNotes = caseData.getCaseNotes().stream()
            .filter(caseNoteElement -> !caseNotesId.contains(caseNoteElement.getId()))
            .collect(toList());

        if (caseData.getCaseNotes().size() - resultCaseNotes.size() != expectedCaseNotesId.size()) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, expected caseNotes id not found",
                migrationId, caseId
            ));
        }

        caseDetails.getData().put("caseNotes", resultCaseNotes);
    }

    private void run1006(CaseDetails caseDetails) {
        var migrationId = "DFPL-1006";
        var expectedCaseId = 1664880596046318L;

        CaseData caseData = getCaseData(caseDetails);
        Long caseId = caseData.getId();

        if (caseId != expectedCaseId) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, expected case id %d",
                migrationId, caseId, expectedCaseId
            ));
        }

        caseDetails.getData().put("state", CASE_MANAGEMENT);
    }

    private final ManageOrderDocumentScopedFieldsCalculator fieldsCalculator;

    private void run1029(CaseDetails caseDetails) {
        // DON'T DELETE THIS MIGRATION, POTENTIALLY ONGOING ISSUES
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

    private void run985(CaseDetails caseDetails) {
        var migrationId = "DFPL-985";
        migrateCaseService.doCaseIdCheck(caseDetails.getId(), 1648203424556112L, migrationId);

        caseDetails.getData().putAll(migrateCaseService.removePositionStatementRespondent(getCaseData(caseDetails),
            migrationId, fromString("5ee0f6f7-1e7c-4fad-a193-b7cb7a6d613d")));
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
}
