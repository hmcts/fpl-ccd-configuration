package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisCoverDocumentsService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendLetterService {

    private static final String SEND_LETTER_TYPE = "FPLA001";
    private static final String COVERSHEET_FILENAME = "Coversheet.pdf";

    private final Time time;
    private final SendLetterApi sendLetterApi;
    private final DocumentDownloadService documentDownloadService;
    private final DocmosisCoverDocumentsService docmosisCoverDocumentsService;
    private final AuthTokenGenerator authTokenGenerator;
    private final UploadDocumentService uploadDocumentService;

    public List<SentDocument> send(DocumentReference mainDocument, List<Recipient> recipients, Long caseId,
                                   String familyManCaseNumber, Language language) {
        List<SentDocument> sentDocuments = new ArrayList<>();
        byte[] mainDocumentBinary = documentDownloadService.downloadDocument(mainDocument.getBinaryUrl());
        var mainDocumentCopy = uploadDocument(mainDocumentBinary, mainDocument.getFilename());

        String mainDocumentEncoded = Base64.getEncoder().encodeToString(mainDocumentBinary);
        for (Recipient recipient : recipients) {
            byte[] coverDocument = docmosisCoverDocumentsService.createCoverDocuments(familyManCaseNumber,
                caseId,
                recipient, language).getBytes();

            String coverDocumentEncoded = Base64.getEncoder().encodeToString(coverDocument);
            var coversheet = uploadDocument(coverDocument, COVERSHEET_FILENAME);
            String letterId = EMPTY;
            try {
                SendLetterResponse response = sendLetterApi.sendLetter(authTokenGenerator.generate(),
                    new LetterWithPdfsRequest(List.of(coverDocumentEncoded, mainDocumentEncoded),
                        SEND_LETTER_TYPE,
                        Map.of("caseId", caseId, "documentName", mainDocument.getFilename())));
                letterId = Optional.ofNullable(response).map(r -> r.letterId.toString()).orElse(EMPTY);
            } catch (Exception exception) {
                log.error("Exception raised when sending letter for case id {} and document {}.",
                    caseId, mainDocument.getFilename(), exception);
            }

            sentDocuments.add(SentDocument.builder()
                .partyName(recipient.getFullName())
                .document(mainDocumentCopy)
                .coversheet(coversheet)
                .sentAt(formatLocalDateTimeBaseUsingFormat(time.now(), "h:mma, d MMMM yyyy"))
                .letterId(letterId)
                .build());
        }

        return sentDocuments;
    }

    private DocumentReference uploadDocument(byte[] documentBinary, String filename) {
        Document uploadedDocument = uploadDocumentService.uploadPDF(documentBinary, filename);

        return buildFromDocument(uploadedDocument);
    }
}
