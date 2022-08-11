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
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;

import java.util.HashMap;
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
        "DFPL-794", this::run794,
        "DFPL-797", this::run797,
        "DFPL-798", this::run798,
        "DFPL-802", this::run802,
        "DFPL-692", this::run692,
        "DFPL-776", this::run776,
        "DFPL-812", this::run812,
        "DFPL-815", this::run815,
        "DFPL-817", this::run817
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
    private void run794(CaseDetails caseDetails) {
        var migrationId = "DFPL-794";
        var expectedCaseId = 1657104996768754L;
        var expectedDocId = UUID.fromString("5da7ae0a-0d53-427f-a538-2cb8c9ea82b6");

        removeC110a(caseDetails, migrationId, expectedCaseId, expectedDocId);
    }

    private void run797(CaseDetails caseDetails) {
        var migrationId = "DFPL-797";
        var expectedCaseId = 1657816793771026L;
        var expectedDocId = UUID.fromString("2cfd676a-665b-4d15-ae9e-5ad2930f75cb");

        removeC110a(caseDetails, migrationId, expectedCaseId, expectedDocId);
    }

    private void run798(CaseDetails caseDetails) {
        var migrationId = "DFPL-798";
        var expectedCaseId = 1654765774567742L;
        var expectedDocId = UUID.fromString("1756656b-6931-467e-8dfe-ac9f152351fe");

        removeC110a(caseDetails, migrationId, expectedCaseId, expectedDocId);
    }

    private void run802(CaseDetails caseDetails) {
        var migrationId = "DFPL-802";
        var expectedCaseId = 1659528630126722L;
        var expectedDocId = UUID.fromString("dcd016c6-a0de-4ed2-91ce-5582a6acaf25");

        removeC110a(caseDetails, migrationId, expectedCaseId, expectedDocId);
    }

    private void run812(CaseDetails caseDetails) {
        var migrationId = "DFPL-812";
        var expectedCaseId = 1659443354639951L;
        var expectedDocId = UUID.fromString("94765768-f635-407b-ad7a-0f285b8e7e8");

        removeC110a(caseDetails, migrationId, expectedCaseId, expectedDocId);
    }

    private void run815(CaseDetails caseDetails) {
        var migrationId = "DFPL-815";
        var expectedCaseId = 1659097273811933L;
        var expectedDocId = UUID.fromString("6157d680-5f23-4b2d-afe6-f3e5abfebad9");

        removeC110a(caseDetails, migrationId, expectedCaseId, expectedDocId);
    }

    private void run817(CaseDetails caseDetails) {
        var migrationId = "DFPL-817";
        var expectedCaseId = 1660050012305169L;
        var expectedDocId = UUID.fromString("b0f69651-9bc6-4e18-af8f-7c636efe60fd");

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
        var expectedCaseId = 1641905747009846L;

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

    private void run776(CaseDetails caseDetails) {
        var migrationId = "DFPL-776";
        var expectedCaseId = 1646318196381762L;

        CaseData caseData = getCaseData(caseDetails);
        Long caseId = caseData.getId();

        if (caseId != expectedCaseId) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, expected case id %d",
                migrationId, caseId, expectedCaseId
            ));
        }

        UUID expectedMsgId = UUID.fromString("878a2dd7-8d50-46b1-88d3-a5c6fe9a39ba");

        List<Element<JudicialMessage>> resultJudicialMessages = caseData.getJudicialMessages().stream()
            .filter(msgElement -> !expectedMsgId.equals(msgElement.getId()))
            .collect(Collectors.toList());

        // only one message should be removed
        if (resultJudicialMessages.size() != caseData.getJudicialMessages().size() - 1) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, invalid JudicialMessage ID",
                migrationId, caseId
            ));
        }

        caseDetails.getData().put("judicialMessages", resultJudicialMessages);
    }
}
