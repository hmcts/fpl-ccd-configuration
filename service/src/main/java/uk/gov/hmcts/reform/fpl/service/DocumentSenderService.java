package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocumentSenderService {

    private static final String SEND_LETTER_SERVICE_AUTH = "fpl_case_service";
    private static final String SEND_LETTER_TYPE = "FPLA001";

    private final Time time;
    private final DateFormatterService dateFormatterService;
    private final SendLetterApi sendLetterApi;
    private final DocumentDownloadService documentDownloadService;
    private final DocmosisCoverDocumentsService docmosisCoverDocumentsService;

    public List<SentDocument> send(DocumentReference mainDocument, List<Representative> representativesServedByPost,
                                   Long caseId, String familyManCaseNumber) {
        List<SentDocument> sentDocuments = new ArrayList<>();
        for (Representative representative : representativesServedByPost) {
            byte[] mainDocumentBinary = documentDownloadService.downloadDocument(mainDocument.getBinaryUrl());
            byte[] coverDocument = docmosisCoverDocumentsService.createCoverDocuments(familyManCaseNumber,
                caseId,
                representative).getBytes();

            sendLetterApi.sendLetter(SEND_LETTER_SERVICE_AUTH,
                new LetterWithPdfsRequest(List.of(coverDocument, mainDocumentBinary), SEND_LETTER_TYPE, Map.of()));

            sentDocuments.add(SentDocument.builder()
                .partyName(representative.getFullName())
                .document(mainDocument)
                .sentAt(dateFormatterService.formatLocalDateTimeBaseUsingFormat(time.now(), "h:mma, d MMMM yyyy"))
                .build());
        }

        return sentDocuments;
    }
}
