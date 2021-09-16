package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisCoverDocumentsService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocumentConversionService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.updateExtension;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendLetterService {

    private static final String SEND_LETTER_TYPE = "FPLA001";
    private static final String COVERSHEET_FILENAME = "Coversheet.pdf";
    private static final String PDF = "pdf";

    private final Time time;
    private final SendLetterApi sendLetterApi;
    private final DocumentDownloadService documentDownloadService;
    private final DocumentConversionService documentConversionService;
    private final DocmosisCoverDocumentsService docmosisCoverDocumentsService;
    private final AuthTokenGenerator authTokenGenerator;
    private final UploadDocumentService uploadDocumentService;

    public List<SentDocument> send(DocumentReference mainDocument, List<Recipient> recipients, Long caseId,
                                   String familyManCaseNumber, Language language) {
        byte[] pdf = getDocumentAsPDF(mainDocument);

        DocumentReference mainDocumentCopy = uploadDocument(pdf, mainDocument.getFilename());
        String mainDocumentEncoded = Base64.getEncoder().encodeToString(pdf);

        return recipients.stream().map(recipient -> {
            byte[] coverDocument = docmosisCoverDocumentsService.createCoverDocuments(
                familyManCaseNumber, caseId, recipient, language
            ).getBytes();

            String coverDocumentEncoded = Base64.getEncoder().encodeToString(coverDocument);
            DocumentReference coversheet = uploadDocument(coverDocument, COVERSHEET_FILENAME);

            SendLetterResponse response = performRequest(
                mainDocument, caseId, mainDocumentEncoded, coverDocumentEncoded
            );

            return SentDocument.builder()
                .partyName(recipient.getFullName())
                .document(mainDocumentCopy)
                .coversheet(coversheet)
                .sentAt(formatLocalDateTimeBaseUsingFormat(time.now(), TIME_DATE))
                .letterId(Optional.ofNullable(response).map(r -> r.letterId.toString()).orElse(EMPTY))
                .build();

        }).collect(Collectors.toList());
    }

    private SendLetterResponse performRequest(DocumentReference mainDocument, Long caseId,
                                              String mainDocumentEncoded, String coverDocumentEncoded) {
        LetterWithPdfsRequest request = new LetterWithPdfsRequest(
            List.of(coverDocumentEncoded, mainDocumentEncoded),
            SEND_LETTER_TYPE,
            Map.of("caseId", caseId, "documentName", getPDFFileName(mainDocument))
        );
        return sendLetterApi.sendLetter(authTokenGenerator.generate(), request);
    }

    // need to convert to pdf for the SendLetter service
    private byte[] getDocumentAsPDF(DocumentReference mainDocument) {
        byte[] mainDocumentBinary = documentDownloadService.downloadDocument(mainDocument.getBinaryUrl());
        return documentConversionService.convertToPdf(mainDocumentBinary, mainDocument.getFilename());
    }

    private DocumentReference uploadDocument(byte[] documentBinary, String filename) {
        Document uploadedDocument = uploadDocumentService.uploadPDF(documentBinary, filename);
        return buildFromDocument(uploadedDocument);
    }

    private String getPDFFileName(DocumentReference document) {
        return updateExtension(document.getFilename(), PDF);
    }
}
