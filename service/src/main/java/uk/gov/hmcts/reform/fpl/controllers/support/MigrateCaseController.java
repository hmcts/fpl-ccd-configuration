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
import uk.gov.hmcts.reform.fpl.model.CaseNote;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {
    private static final String MIGRATION_ID_KEY = "migrationId";

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-753", this::run753,
        "DFPL-754", this::run754,
        "DFPL-755", this::run755,
        "DFPL-692", this::run692
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

    /**
     * Removes a C110A Generated PDF document from the case.
     * Make sure to update:
     *  - expectedCaseId
     *  - expectedDocId
     *  - migrationId
     * @param caseDetails - the caseDetails to update
     */
    private void run753(CaseDetails caseDetails) {
        var migrationId = "DFPL-753";
        var expectedCaseId = 1655475144643137L;
        var expectedDocId = UUID.fromString("e7cff38c-b831-42ac-b45a-aca7ea20a406");

        removeC110a(caseDetails, migrationId, expectedCaseId, expectedDocId);
    }

    private void run754(CaseDetails caseDetails) {
        var migrationId = "DFPL-754";
        var expectedCaseId = 1656080425565600L;
        var expectedDocId = UUID.fromString("5ce6ec53-e167-4480-a878-7e78f15f9007");

        removeC110a(caseDetails, migrationId, expectedCaseId, expectedDocId);
    }

    private void run755(CaseDetails caseDetails) {
        var migrationId = "DFPL-755";
        var expectedCaseId = 1652867432494707L;
        var expectedDocId = UUID.fromString("d696d4a6-85f7-449b-9127-7d8a8b42bac5");

        removeC110a(caseDetails, migrationId, expectedCaseId, expectedDocId);
    }

    private void removeC110a(CaseDetails caseDetails, String migrationId, long expectedCaseId, UUID expectedDocId) {
        CaseData caseData = getCaseData(caseDetails);
        var caseId = caseData.getId();

        if (caseId != expectedCaseId) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, expected case id %d",
                migrationId, caseId, expectedCaseId
            ));
        }

        var documentUrl = caseData.getC110A().getDocument().getUrl();
        var docId = UUID.fromString(documentUrl.substring(documentUrl.length() - 36));
        if (!docId.equals(expectedDocId)) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, expected c110a document id %s",
                migrationId, caseId, expectedDocId
            ));
        }
        caseDetails.getData().put("submittedForm", null);
    }

    private void run692(CaseDetails caseDetails) {
        var migrationId = "DFPL-692";
        var expectedCaseId = 1658501387557002L;

        CaseData caseData = getCaseData(caseDetails);
        Long caseId = caseData.getId();

        if (caseId != expectedCaseId) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, expected case id %d",
                migrationId, caseId, expectedCaseId
            ));
        }

        List<UUID> expectedNotesId = List.of(UUID.fromString("7dd3c2ac-d49f-4119-8299-a19a62f1d6db"),
            UUID.fromString("66fb7c25-7860-4a5c-98d4-dd2ff575eb28"));

        List<Element<CaseNote>> resultCaseNotes = caseData.getCaseNotes().stream()
            .filter(caseNoteElement -> !expectedNotesId.contains(caseNoteElement.getId()))
            .collect(Collectors.toList());

        if (caseData.getCaseNotes().size() - resultCaseNotes.size() != 2) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, expected caseNotes id not found",
                migrationId, caseId
            ));
        }

        caseDetails.getData().put("caseNotes", resultCaseNotes);
    }
}
