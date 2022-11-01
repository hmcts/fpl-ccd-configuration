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
import uk.gov.hmcts.reform.fpl.model.*;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {
    private static final String MIGRATION_ID_KEY = "migrationId";

    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-798", this::run798,
        "DFPL-802", this::run802,
        "DFPL-776", this::run776,
        "DFPL-826", this::run826,
        "DFPL-810", this::run810,
        "DFPL-828", this::run828,
        "DFPL-809a", this::run809a,
        "DFPL-809b", this::run809b
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

    private void run809a(CaseDetails caseDetails) {
        var migrationId = "DFPL-809a";
        var expectedCaseId = 1651569615587841L;

        removeConfidentialTab(caseDetails, migrationId, expectedCaseId);
    }

    private void run809b(CaseDetails caseDetails) {
        var migrationId = "DFPL-809b";
        var expectedCaseId = 1651755091217652L;

        removeConfidentialTab(caseDetails, migrationId, expectedCaseId);
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

    private void removeConfidentialTab(CaseDetails caseDetails, String migrationId, long expectedCaseId) {
        CaseData caseData = getCaseData(caseDetails);
        var caseId = caseData.getId();

        if (caseId != expectedCaseId) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, expected case id %d",
                migrationId, caseId, expectedCaseId
            ));
        }

        List<Element<SupportingEvidenceBundle>> correspondenceDocuments = caseData.getCorrespondenceDocuments();
        List<Element<SupportingEvidenceBundle>> newCorrespondenceDocuments = new ArrayList<>();

        for (Element<SupportingEvidenceBundle> bundle : correspondenceDocuments) {
            List<String> confidentialTag = bundle.getValue().getConfidential();
            String hasConfidentialAddress = bundle.getValue().getHasConfidentialAddress();

            if (nonNull(confidentialTag)) {
                if (confidentialTag.contains("CONFIDENTIAL") && hasConfidentialAddress.equals("No")) {
                    newCorrespondenceDocuments.add(element(SupportingEvidenceBundle.builder()
                        .name(bundle.getValue().getName())
                        .document(bundle.getValue().getDocument())
                        .uploadedBy(bundle.getValue().getUploadedBy())
                        .confidential(emptyList())
                        .dateTimeUploaded(bundle.getValue().getDateTimeUploaded())
                        .hasConfidentialAddress(bundle.getValue().getHasConfidentialAddress())
                        .build()));
                } else {
                    newCorrespondenceDocuments.add(bundle);
                }
            } else {
                newCorrespondenceDocuments.add(bundle);
            }
        }

        caseDetails.getData().put("correspondenceDocuments", newCorrespondenceDocuments);
    }

    private void run810(CaseDetails caseDetails) {
        var migrationId = "DFPL-810";
        var expectedCaseId = 1639481593478877L;
        var expectedCaseNotesId = List.of("2824e43b-3250-485a-b069-6fd06390ce83");

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

    private void run828(CaseDetails caseDetails) {
        removeDocumentSentToParty(caseDetails, 1651850415891595L, "DFPL-828",
            "f3264cc6-61b7-4cf7-ab37-c9eb35a13e03",
            List.of("6fcc6eb4-942d-40e1-bfa6-befd70254f7f",
                "fbea9e67-8244-43b8-97c5-265da7b9b6c7",
                "518a9339-786c-4b68-bce9-a064616ac47f",
                "61733660-67ed-4ea7-8711-2fe80e15af68"));
    }

    private void removeDocumentSentToParty(CaseDetails caseDetails, long expectedCaseId,
                                          String migrationId,
                                          String expectedDocumentsSentToPartiesId,
                                          List<String> docIdsToBeRemoved) {
        CaseData caseData = getCaseData(caseDetails);
        Long caseId = caseData.getId();

        if (caseId != expectedCaseId) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, expected case id %d",
                migrationId, caseId, expectedCaseId
            ));
        }

        final UUID expectedPartyUuid = UUID.fromString(expectedDocumentsSentToPartiesId);
        final List<UUID> docUuidsToBeRemoved = docIdsToBeRemoved.stream().map(UUID::fromString).collect(toList());

        final Element<SentDocuments> targetDocumentsSentToParties = ElementUtils.findElement(expectedPartyUuid,
                caseData.getDocumentsSentToParties())
            .orElseThrow(() -> new AssertionError(format(
                "Migration {id = %s, case reference = %s}, party Id not found",
                migrationId, caseId)));

        docUuidsToBeRemoved.stream().forEach(docIdToBeRemoved -> {
                if (ElementUtils.findElement(docIdToBeRemoved,
                        targetDocumentsSentToParties.getValue().getDocumentsSentToParty()).isEmpty()) {
                    throw new AssertionError(format(
                        "Migration {id = %s, case reference = %s}, document Id not found",
                        migrationId, caseId));
                }
            }
        );

        final List<Element<SentDocuments>> resultDocumentsSentToParties = caseData.getDocumentsSentToParties().stream()
            .map(documentsSentToParty -> {
                if (!expectedPartyUuid.equals(documentsSentToParty.getId())) {
                    return documentsSentToParty;
                } else {
                    return element(documentsSentToParty.getId(),
                        documentsSentToParty.getValue().toBuilder()
                            .documentsSentToParty(documentsSentToParty.getValue().getDocumentsSentToParty().stream()
                                .filter(documentSent -> !docUuidsToBeRemoved.contains(documentSent.getId()))
                                .collect(Collectors.toList())).build());
                }
            }).collect(Collectors.toList());

        caseDetails.getData().put("documentsSentToParties", resultDocumentsSentToParties);
    }
}
