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
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.SentDocuments;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {
    private static final String MIGRATION_ID_KEY = "migrationId";

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-500", this::run500,
        "DFPL-466", this::run466,
        "DFPL-482", this::run482,
        "DFPL-551", this::run551
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

    private void run466(CaseDetails caseDetails) {
        var caseId = caseDetails.getId();
        if (caseId != 1611613172339094L) {
            throw new AssertionError(format(
                "Migration {id = DFPL-466, case reference = %s}, expected case id 1611613172339094",
                caseId
            ));
        }

        if (isNotEmpty(caseDetails.getData().get("hearingOption"))) {
            caseDetails.getData().remove("hearingOption");
        } else {
            throw new IllegalStateException(format("Case %s does not have hearing option", caseId));
        }
    }

    private void run500(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        var caseId = caseData.getId();
        List<UUID> docIds = List.of(UUID.fromString("ad5c738e-d7aa-4ccf-b53b-0b1e40a19182"),
                UUID.fromString("61f97374-360b-4759-9329-af10fae1317e"));

        if (caseId != 1643728359576136L) {
            throw new AssertionError(format(
                    "Migration {id = DFPL-500, case reference = %s}, expected case id 1643728359576136",
                    caseId
            ));
        }

        updateDocumentsSentToParties(caseDetails, caseData, docIds);
    }

    private void run482(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        var caseId = caseData.getId();
        var expectedCaseId = 1636970654155393L;
        List<UUID> docIds = List.of(UUID.fromString("75dcdc34-7f13-4c56-aad6-8dcf7b2261b6"),
                UUID.fromString("401d9cd0-50ae-469d-b355-d467742d7ef3"));

        if (caseId != expectedCaseId) {
            throw new AssertionError(format(
                    "Migration {id = DFPL-482, case reference = %s}, expected case id %d",
                    caseId, expectedCaseId
            ));
        }

        updateDocumentsSentToParties(caseDetails, caseData, docIds);
    }

    private void run551(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        var caseId = caseData.getId();
        var expectedCaseId = 1641312631808724L;

        var expectedDocId = UUID.fromString("0de3ba95-e383-41b4-8a66-52bf0dce834e");

        if (caseId != expectedCaseId) {
            throw new AssertionError(format(
                "Migration {id = DFPL-551, case reference = %s}, expected case id %d",
                caseId, expectedCaseId
            ));
        }

        var documentUrl = caseData.getC110A().getDocument().getUrl();
        var docId = UUID.fromString(documentUrl.substring(documentUrl.length() - 36));
        if (!docId.equals(expectedDocId)) {
            throw new AssertionError(format(
                "Migration {id = DFPL-551, case reference = %s}, expected c110a document id %s",
                caseId, expectedDocId
            ));
        }
        caseDetails.getData().put("submittedForm", null);
    }

    private void updateDocumentsSentToParties(CaseDetails caseDetails, CaseData caseData, List<UUID> docIds) {
        List<Element<SentDocuments>> sentDocuments = caseData.getDocumentsSentToParties();

        for (Element<SentDocuments> docsSentToParties : sentDocuments) {
            List<Element<SentDocument>> filteredList =
                getDocToRemove(docsSentToParties.getValue().getDocumentsSentToParty(),
                    docIds);
            docsSentToParties.getValue().getDocumentsSentToParty().removeAll(filteredList);
        }

        caseDetails.getData().put("documentsSentToParties", sentDocuments);
    }

    private List<Element<SentDocument>> getDocToRemove(List<Element<SentDocument>> sentDocument,
                                                       List<UUID> docIds) {
        return sentDocument.stream()
                .filter(element -> docIds.contains(element.getId()))
                .collect(Collectors.toList());
    }

}
