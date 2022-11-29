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
import uk.gov.hmcts.reform.fpl.model.PositionStatementRespondent;
import uk.gov.hmcts.reform.fpl.model.RespondentStatement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.service.document.DocumentListService;
import uk.gov.hmcts.reform.fpl.service.MigrateCaseService;
import uk.gov.hmcts.reform.fpl.service.orders.ManageOrderDocumentScopedFieldsCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Consumer;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.UUID.fromString;
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
        "DFPL-985", this::run985,
        "DFPL-1012", this::run1012,
        "DFPL-776", this::run776,
        "DFPL-1015", this::run1015,
        "DFPL-809a", this::run809a,
        "DFPL-809b", this::run809b,
        "DFPL-979", this::run979,
        "DFPL-1006", this::run1006,
        "DFPL-969", this::run969,
        "DFPL-1034", this::run1034
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

    private void run1015(CaseDetails caseDetails) {
        removeHearingBooking("DFPL-1015", 1641373238062313L, caseDetails,
            "894fa026-e403-45e8-a2fe-105e8135ee5b");
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

    private void run1034(CaseDetails caseDetails) {
        var migrationId = "DFPL-1034";
        migrateCaseService.doCaseIdCheck(caseDetails.getId(), 1667917072654848L, migrationId);

        caseDetails.getData().putAll(migrateCaseService.removeDocumentsSentToParties(getCaseData(caseDetails),
            migrationId,
            fromString("a1ba061a-2982-4ce3-9881-b74c37aa9b4f"),
            List.of(fromString("0e133c81-51aa-4bd3-b5aa-7689224ad28a"))));
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

    private void run969(CaseDetails caseDetails) {
        var migrationId = "DFPL-969";

        migrateCaseService.doCaseIdCheck(caseDetails.getId(), 1654525609722908L, migrationId);

        caseDetails.getData().putAll(migrateCaseService.removeHearingOrderBundleDraft(getCaseData(caseDetails),
            migrationId, UUID.fromString("4f20eca8-d255-4339-bb09-23a1e2ba7d80"),
            UUID.fromString("84573155-34ac-4ff4-b616-54ac4cc369cb")));
    }

    private final DocumentListService documentListService;

    private void removeRespondentStatementList(CaseDetails caseDetails, long expectedCaseId,
                                               String migrationId,
                                               String expectedRespondentStatementId) {
        CaseData caseData = getCaseData(caseDetails);
        final Long caseId = caseData.getId();

        if (caseId != expectedCaseId) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, expected case id %d",
                migrationId, caseId, expectedCaseId));
        }

        List<Element<RespondentStatement>> respondentStatementsResult =
            caseData.getRespondentStatements().stream()
                .filter(el -> !el.getId().equals(UUID.fromString(expectedRespondentStatementId)))
                .collect(toList());

        if (respondentStatementsResult.size() != caseData.getRespondentStatements().size() - 1) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, invalid respondent statements",
                migrationId, caseId));
        }

        caseDetails.getData().put("respondentStatements", respondentStatementsResult);
        // refreshing the document view
        caseDetails.getData().putAll(documentListService.getDocumentView(getCaseData(caseDetails)));
    }

    private void removePositionStatementRespondentList(CaseDetails caseDetails, long expectedCaseId,
                                                       String migrationId,
                                                       String expectedPositionStatementId) {
        CaseData caseData = getCaseData(caseDetails);
        final Long caseId = caseData.getId();

        if (caseId != expectedCaseId) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, expected case id %d",
                migrationId, caseId, expectedCaseId));
        }

        List<Element<PositionStatementRespondent>> positionStatementRespondentListResult =
            caseData.getHearingDocuments().getPositionStatementRespondentListV2().stream()
                .filter(el -> !el.getId().equals(UUID.fromString(expectedPositionStatementId)))
                .collect(toList());

        if (positionStatementRespondentListResult.size() != caseData.getHearingDocuments()
            .getPositionStatementRespondentListV2().size() - 1) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, invalid position statement respondent",
                migrationId, caseId));
        }

        caseDetails.getData().put("positionStatementRespondentListV2", positionStatementRespondentListResult);
    }

    private void run985(CaseDetails caseDetails) {
        final String migrationId = "DFPL-985";
        final Long expectedCaseId = 1648203424556112L;
        removeRespondentStatementList(caseDetails, expectedCaseId, migrationId,
            "4b88563e-c6b3-4780-90b6-531e1db65b7e");
    }

    private void run1012(CaseDetails caseDetails) {
        final String migrationId = "DFPL-1012";
        final Long expectedCaseId = 1661877618161045L;
        removePositionStatementRespondentList(caseDetails, expectedCaseId, migrationId,
            "b8da3a48-441f-4210-a21c-7008d256aa32");
    }
}
