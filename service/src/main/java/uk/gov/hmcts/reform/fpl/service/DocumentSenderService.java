package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.service.DateFormatterService.formatLocalDateTimeBaseUsingFormat;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocumentSenderService {

    private static final String SEND_LETTER_TYPE = "FPLA001";

    private final Time time;
    private final SendLetterApi sendLetterApi;
    private final DocumentDownloadService documentDownloadService;
    private final DocmosisCoverDocumentsService docmosisCoverDocumentsService;
    private final AuthTokenGenerator authTokenGenerator;

    public List<SentDocument> send(DocumentReference mainDocument, List<Representative> representativesServedByPost,
                                   Long caseId, String familyManCaseNumber) {
        List<SentDocument> sentDocuments = new ArrayList<>();
        byte[] mainDocumentBinary = documentDownloadService.downloadDocument(mainDocument.getBinaryUrl());
        for (Representative representative : representativesServedByPost) {
            byte[] coverDocument = docmosisCoverDocumentsService.createCoverDocuments(familyManCaseNumber,
                caseId,
                representative).getBytes();

            sendLetterApi.sendLetter(authTokenGenerator.generate(),
                new LetterWithPdfsRequest(List.of(coverDocument, mainDocumentBinary), SEND_LETTER_TYPE, Map.of()));

            sentDocuments.add(SentDocument.builder()
                .partyName(representative.getFullName())
                .document(mainDocument)
                .sentAt(formatLocalDateTimeBaseUsingFormat(time.now(), "h:mma, d MMMM yyyy"))
                .build());
        }

        return sentDocuments;
    }
}
