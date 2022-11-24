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
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.SentDocuments;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.service.MigrateCaseService;
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrderDocumentScopedFieldsCalculator;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
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
        "DFPL-776", this::run776,
        "DFPL-1001", this::run1001,
        "DFPL-809a", this::run809a,
        "DFPL-809b", this::run809b,
        "DFPL-979", this::run979,
        "DFPL-982", this::run982,
        "DFPL-1006", this::run1006,
        "DFPL-1029", this::run1029,
        "DFPL-969", this::run969
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

    private void run1001(CaseDetails caseDetails) {
        removeHearingBooking("DFPL-1001", 1649335087796806L, caseDetails,
            "9cc3f847-3f2c-4d19-bf32-ed1377866ffe");
    }

    private void removeHearingBooking(final String migrationId, final Long expectedCaseId,
                                      final CaseDetails caseDetails, final String hearingIdToBeRemoved) {

        CaseData caseData = getCaseData(caseDetails);
        final Long caseId = caseData.getId();
        final UUID expectedHearingId = UUID.fromString(hearingIdToBeRemoved);

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
        var migrationId = "DFPL-1029";
        var expectedCaseId = 1650359065299290L;

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

    private void run982(CaseDetails caseDetails) {
        removeDocumentSentToParty(caseDetails, 1661249570230673L, "DFPL-982",
            "52a06d8d-283b-446a-b4e8-64bba3a54f7f",
            List.of("f6d74661-e3d8-4d0d-9ee3-09bdf0068dd2",
                "a3755cb6-4e12-4670-8779-c07e00ec669e"));
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

    private void run969(CaseDetails caseDetails) {
        var migrationId = "DFPL-969";

        migrateCaseService.doCaseIdCheck(caseDetails.getId(), 1654525609722908L, migrationId);

        caseDetails.getData().putAll(migrateCaseService.removeHearingOrderBundleDraft(getCaseData(caseDetails),
            migrationId, UUID.fromString("4f20eca8-d255-4339-bb09-23a1e2ba7d80"),
            UUID.fromString("84573155-34ac-4ff4-b616-54ac4cc369cb")));
    }

}
