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
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.document.DocumentListService;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {
    private static final String MIGRATION_ID_KEY = "migrationId";

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-733", this::run733,
        "DFPL-734", this::run734,
        "DFPL-735", this::run735,
        "DFPL-736", this::run736,
        "DFPL-726", this::run726,
        "DFPL-725", this::run725
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
    private void run733(CaseDetails caseDetails) {
        var migrationId = "DFPL-733";
        var expectedCaseId = 1655396292296801L;
        var expectedDocId = UUID.fromString("854f3df6-765b-4ee9-a6b4-eedc702837e0");

        removeC110a(caseDetails, migrationId, expectedCaseId, expectedDocId);
    }

    private void run734(CaseDetails caseDetails) {
        var migrationId = "DFPL-734";
        var expectedCaseId = 1653312644320480L;
        var expectedDocId = UUID.fromString("b86a46b9-84d3-4a05-a4eb-16daaad75a89");

        removeC110a(caseDetails, migrationId, expectedCaseId, expectedDocId);
    }

    private void run735(CaseDetails caseDetails) {
        var migrationId = "DFPL-735";
        var expectedCaseId = 1655286118759043L;
        var expectedDocId = UUID.fromString("2765508d-8dc5-41ab-bdeb-1082597c3628");

        removeC110a(caseDetails, migrationId, expectedCaseId, expectedDocId);
    }

    private void run736(CaseDetails caseDetails) {
        var migrationId = "DFPL-736";
        var expectedCaseId = 1656319106830085L;
        var expectedDocId = UUID.fromString("9ef09ed4-b208-4166-af6a-1c741bb676d1");

        removeC110a(caseDetails, migrationId, expectedCaseId, expectedDocId);
    }

    private void run726(CaseDetails caseDetails) {
        var migrationId = "DFPL-726";
        var expectedCaseId = 1651829414420283L;
        var expectedDocId = UUID.fromString("df338226-8816-453a-acc5-baba31712a0c");

        removeC110a(caseDetails, migrationId, expectedCaseId, expectedDocId);
    }

    private void run725(CaseDetails caseDetails) {
        var migrationId = "DFPL-725";
        var expectedCaseId = 1654773142311280L;
        var expectedDocId = UUID.fromString("79e7dbe4-71b7-40c3-81f4-a8954c5b8bed");

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
}
