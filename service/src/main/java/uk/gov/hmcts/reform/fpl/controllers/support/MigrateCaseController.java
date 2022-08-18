package uk.gov.hmcts.reform.fpl.controllers.support;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApiV2;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.CallbackController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseNote;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.request.RequestData;

import java.util.HashMap;
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

    private final CoreCaseDataApiV2 coreCaseDataApi;

    private final RequestData requestData;

    private final AuthTokenGenerator authToken;

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-794", this::run794,
        "DFPL-797", this::run797,
        "DFPL-798", this::run798,
        "DFPL-802", this::run802,
        "DFPL-692", this::run692,
        "DFPL-776", this::run776,
        "DFPL-826", this::run826,
        "DFPL-702", this::run702
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

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        final CaseData caseData = getCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        String migrationId = (String) caseDetails.getData().get(MIGRATION_ID_KEY);
        Long id = caseDetails.getId();

        if ("DFPL-702".equals(migrationId)) {
            // update supplementary data
            String caseId = caseData.getId().toString();
            Map<String, Map<String, Map<String, Object>>> supplementaryData = new HashMap<>();
            supplementaryData.put("supplementary_data_updates",
                Map.of("$set", Map.of("HMCTSServiceId", "ABA3")));
            coreCaseDataApi.submitSupplementaryData(requestData.authorisation(), authToken.generate(), caseId,
                supplementaryData);
        }
        caseDetails.getData().remove(MIGRATION_ID_KEY);
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

    private void run702(CaseDetails caseDetails) {
        var migrationId = "DFPL-702";

        CaseData caseData = getCaseData(caseDetails);
        String caseName = caseData.getCaseName();

        // migrating top level fields: case names
        caseDetails.getData().put("caseNameHmctsInternal", caseName);
        // migrating caseManagementLocation TODO
        caseDetails.getData().put("caseManagementCategory", DynamicList.builder()
            .value(DynamicListElement.builder().code("FPL").label("Family Public Law").build())
            .listItems(List.of(
                DynamicListElement.builder().code("FPL").label("Family Public Law").build()
            ))
            .build());
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
            .collect(toList());

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
            .collect(toList());

        // only one message should be removed
        if (resultJudicialMessages.size() != caseData.getJudicialMessages().size() - 1) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, invalid JudicialMessage ID",
                migrationId, caseId
            ));
        }

        caseDetails.getData().put("judicialMessages", resultJudicialMessages);
    }

    private void run826(CaseDetails caseDetails) {
        final String migrationId = "DFPL-826";
        CaseData caseData = getCaseData(caseDetails);
        final Long caseId = caseData.getId();
        final Long expectedCaseId = 1660300177298257L;
        final UUID expectedHearingId = UUID.fromString("d76c0df0-2fe3-4ee7-aafa-3703bdc5b7e0");

        if (!expectedCaseId.equals(caseId)) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, expected case id %d",
                migrationId, caseId, expectedCaseId
            ));
        }

        List<Element<HearingBooking>> hearingDetails = caseData.getHearingDetails();
        if (hearingDetails != null) {
            // get the hearing with the expected UUID
            List<Element<HearingBooking>> hearingBookingsToBeRemoved =
                hearingDetails.stream().filter(hearingBooking -> expectedHearingId.equals(hearingBooking.getId()))
                    .collect(toList());

            if (hearingBookingsToBeRemoved.size() == 0) {
                throw new AssertionError(format(
                    "Migration {id = %s, case reference = %s}, hearing booking %s not found",
                    migrationId, caseId, expectedHearingId
                ));
            }

            if (hearingBookingsToBeRemoved.size() > 1) {
                throw new AssertionError(format(
                    "Migration {id = %s, case reference = %s}, more than one hearing booking %s found",
                    migrationId, caseId, expectedHearingId
                ));
            }

            // remove the hearing from the hearing list
            hearingDetails.removeAll(hearingBookingsToBeRemoved);
            caseDetails.getData().put("hearingDetails", hearingDetails);
            caseDetails.getData().put("selectedHearingId", hearingDetails.get(hearingDetails.size() - 1).getId());
        } else {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, hearing details not found",
                migrationId, caseId
            ));
        }
    }
}
