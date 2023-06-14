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
import uk.gov.hmcts.reform.fpl.model.ManagedDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.MigrateCaseService;
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrderDocumentScopedFieldsCalculator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Consumer;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {
    private static final String MIGRATION_ID_KEY = "migrationId";

    private final MigrateCaseService migrateCaseService;
    private final ManageOrderDocumentScopedFieldsCalculator fieldsCalculator;

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-1359", this::run1359,
        "DFPL-1401", this::run1401,
        "DFPL-1451", this::run1451,
        "DFPL-1466", this::run1466,
        "DFPL-1501", this::run1501,
        "DFPL-1484", this::run1484,
        "DFPL-1500", this::run1500
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

    private void run1359(CaseDetails caseDetails) {
        migrateCaseService.doDocumentViewNCCheck(caseDetails.getId(), "DFPL-1359", caseDetails);
        caseDetails.getData().putAll(migrateCaseService.refreshDocumentViews(getCaseData(caseDetails)));
    }

    private void run1401(CaseDetails caseDetails) {
        var migrationId = "DFPL-1401";
        var possibleCaseIds = List.of(1666959378667166L);
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        caseDetails.getData().put("relatingLA", "NCC");
    }

    private void run1451(CaseDetails caseDetails) {
        var migrationId = "DFPL-1451";
        var possibleCaseIds = List.of(1669909306379829L);
        final UUID expectedOrderId = UUID.fromString("c93a824b-75ce-4ffd-ad30-ad7f42c01ed9");
        final UUID expectedHearingOrderBundleId = UUID.fromString("ebdf7ea7-a2e8-4296-be98-109b9070348e");
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        caseDetails.getData().putAll(migrateCaseService.removeDraftUploadedCMOs(getCaseData(caseDetails),
            migrationId, expectedOrderId));
        caseDetails.getData().putAll(migrateCaseService.removeHearingOrdersBundlesDrafts(getCaseData(caseDetails),
            migrationId, expectedHearingOrderBundleId));
    }

    private void run1466(CaseDetails caseDetails) {
        var migrationId = "DFPL-1466";
        var possibleCaseIds = List.of(1665396049325141L);
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);

        caseDetails.getData().putAll(migrateCaseService.removePositionStatementChild(getCaseData(caseDetails),
            migrationId, UUID.fromString("b8da3a48-441f-4210-a21c-7008d256aa32")));
    }

    private void run1501(CaseDetails caseDetails) {
        var migrationId = "DFPL-1501";
        var possibleCaseIds = List.of(1659711594032934L);

        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        caseDetails.getData().putAll(migrateCaseService.removeFurtherEvidenceSolicitorDocuments(
            getCaseData(caseDetails), migrationId, UUID.fromString("43a9287c-f840-4104-958f-cbd98d28aea3")));
    }  
  
    private void run1484(CaseDetails caseDetails) {
        var migrationId = "DFPL-1484";
        var possibleCaseIds = List.of(1681381038761399L);
        final UUID hearingId = UUID.fromString("1a41582a-57f5-4802-90b6-949f15ee5875");
        final UUID courtBundleId = UUID.fromString("edc59f83-5e96-4fa2-809a-f34ba71a1204");
        migrateCaseService.doCaseIdCheckList(caseDetails.getId(), possibleCaseIds, migrationId);
        caseDetails.getData().putAll(migrateCaseService.removeCourtBundleByBundleId(getCaseData(caseDetails),
            migrationId, hearingId, courtBundleId));
    }

    private void run1500(CaseDetails caseDetails) {
        // for QA testing purpose
        Map<String, String> fieldNameToFilename = new HashMap<>();
        fieldNameToFilename.put("parentAssessmentList", "parent-assessment");
        fieldNameToFilename.put("famAndViabilityList", "family-and-viability");
        fieldNameToFilename.put("applicantOtherDocList", "applicant-other-doc");
        fieldNameToFilename.put("meetingNoteList", "meeting-note");
        fieldNameToFilename.put("contactNoteList", "contact-note");
        fieldNameToFilename.put("judgementList", "judgement");
        fieldNameToFilename.put("transcriptList", "transcript");
        fieldNameToFilename.put("respWitnessStmtList", "respondent-witness-statement");

        fieldNameToFilename.entrySet().stream().forEach(e -> {
            caseDetails.getData().put(e.getKey(), List.of(
                element(UUID.randomUUID(), ManagedDocument.builder()
                    .document(DocumentReference.builder()
                        .url(format("http://dm-store-aat.service.core-compute-aat.internal/documents/%s",
                            "6e4efc77-1906-4906-b0ca-5154155db1a6"))
                        .binaryUrl(format("http://dm-store-aat.service.core-compute-aat.internal/documents/%s/binary",
                            "6e4efc77-1906-4906-b0ca-5154155db1a6"))
                        .filename(format("non-confidential-%s.pdf", e.getValue())).build())
                    .build())));

            caseDetails.getData().put(e.getKey() + "LA", List.of(
                element(UUID.randomUUID(), ManagedDocument.builder()
                    .document(DocumentReference.builder()
                        .url(format("http://dm-store-aat.service.core-compute-aat.internal/documents/%s",
                            "6e4efc77-1906-4906-b0ca-5154155db1a6"))
                        .binaryUrl(format("http://dm-store-aat.service.core-compute-aat.internal/documents/%s/binary",
                            "6e4efc77-1906-4906-b0ca-5154155db1a6"))
                        .filename(format("la-confidential-%s.pdf", e.getValue())).build())
                    .build())));

            caseDetails.getData().put(e.getKey() + "CTSC", List.of(
                element(UUID.randomUUID(), ManagedDocument.builder()
                    .document(DocumentReference.builder()
                        .url(format("http://dm-store-aat.service.core-compute-aat.internal/documents/%s",
                            "6e4efc77-1906-4906-b0ca-5154155db1a6"))
                        .binaryUrl(format("http://dm-store-aat.service.core-compute-aat.internal/documents/%s/binary",
                            "6e4efc77-1906-4906-b0ca-5154155db1a6"))
                        .filename(format("ctsc-confidential-%s.pdf", e.getValue())).build())
                    .build())));

            caseDetails.getData().put(e.getKey() + "Removed", List.of(
                element(UUID.randomUUID(), ManagedDocument.builder()
                    .document(DocumentReference.builder()
                        .url(format("http://dm-store-aat.service.core-compute-aat.internal/documents/%s",
                            "6e4efc77-1906-4906-b0ca-5154155db1a6"))
                        .binaryUrl(format("http://dm-store-aat.service.core-compute-aat.internal/documents/%s/binary",
                            "6e4efc77-1906-4906-b0ca-5154155db1a6"))
                        .filename(format("removed-%s.pdf", e.getValue())).build())
                    .build())));
        });
    }
}
