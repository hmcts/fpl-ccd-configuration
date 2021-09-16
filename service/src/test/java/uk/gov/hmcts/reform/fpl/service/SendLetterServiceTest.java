package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisCoverDocumentsService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocumentConversionService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTime;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinaries;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testRepresentative;

@ExtendWith(MockitoExtension.class)
class SendLetterServiceTest {

    private static final String SERVICE_AUTH_TOKEN = "Service token";
    private static final String CASE_NUMBER = "familyCaseNumber";
    private static final String COVERSHEET_NAME = "Coversheet.pdf";
    private static final Long CASE_ID = 1L;
    private static final String MAIN_DOCUMENT_FILE_NAME = "file.pdf";
    private static final DocumentReference MAIN_DOCUMENT_REFERENCE = testDocumentReference(MAIN_DOCUMENT_FILE_NAME);
    private static final byte[] MAIN_DOCUMENT_BYTES = testDocumentBinaries();
    private static final String MAIN_DOCUMENT_ENCODED = Base64.getEncoder().encodeToString(MAIN_DOCUMENT_BYTES);
    private static final Document UPLOADED_MAIN_DOCUMENT = testDocument();
    private static final List<byte[]> COVER_DOCUMENTS_BYTES = List.of(new byte[] {0}, new byte[] {1});
    private static final List<String> COVER_DOCUMENTS_ENCODED = List.of(
        Base64.getEncoder().encodeToString(COVER_DOCUMENTS_BYTES.get(0)),
        Base64.getEncoder().encodeToString(COVER_DOCUMENTS_BYTES.get(1))
    );
    private static final List<Document> COVERSHEETS = List.of(testDocument(), testDocument());
    private static final List<Recipient> RECIPIENTS = List.of(testRepresentative(), testRepresentative());
    private static final List<UUID> LETTERS_IDS = List.of(UUID.randomUUID(), UUID.randomUUID());

    private final Time time = new FixedTime(LocalDateTime.of(1,1,1,1,1,1));
    private final SendLetterApi sendLetterApi = mock(SendLetterApi.class);
    private final DocumentDownloadService downloadService = mock(DocumentDownloadService.class);
    private final DocumentConversionService conversionService = mock(DocumentConversionService.class);
    private final DocmosisCoverDocumentsService coverDocumentsService = mock(DocmosisCoverDocumentsService.class);
    private final AuthTokenGenerator authTokenGenerator = mock(AuthTokenGenerator.class);
    private final UploadDocumentService uploadService = mock(UploadDocumentService.class);

    private SendLetterService underTest;

    @Captor
    private ArgumentCaptor<LetterWithPdfsRequest> letterWithPdfsRequestArgumentCaptor;

    @BeforeEach
    void setup() {
        underTest = new SendLetterService(
            time, sendLetterApi, downloadService, conversionService, coverDocumentsService, authTokenGenerator,
            uploadService
        );

        given(conversionService.convertToPdf(MAIN_DOCUMENT_BYTES, MAIN_DOCUMENT_FILE_NAME))
            .willReturn(MAIN_DOCUMENT_BYTES);
        given(uploadService.uploadPDF(MAIN_DOCUMENT_BYTES, MAIN_DOCUMENT_FILE_NAME))
            .willReturn(UPLOADED_MAIN_DOCUMENT);
        given(uploadService.uploadPDF(COVER_DOCUMENTS_BYTES.get(0), COVERSHEET_NAME))
            .willReturn(COVERSHEETS.get(0));
        given(uploadService.uploadPDF(COVER_DOCUMENTS_BYTES.get(1), COVERSHEET_NAME))
            .willReturn(COVERSHEETS.get(1));
        given(sendLetterApi.sendLetter(anyString(), any(LetterWithPdfsRequest.class)))
            .willReturn(new SendLetterResponse(LETTERS_IDS.get(0)))
            .willReturn(new SendLetterResponse(LETTERS_IDS.get(1)));
        given(downloadService.downloadDocument(MAIN_DOCUMENT_REFERENCE.getBinaryUrl()))
            .willReturn(MAIN_DOCUMENT_BYTES);
        given(coverDocumentsService.createCoverDocuments(CASE_NUMBER, CASE_ID, RECIPIENTS.get(0), Language.ENGLISH))
            .willReturn(testDocmosisDocument(COVER_DOCUMENTS_BYTES.get(0)));
        given(coverDocumentsService.createCoverDocuments(CASE_NUMBER, CASE_ID, RECIPIENTS.get(1), Language.ENGLISH))
            .willReturn(testDocmosisDocument(COVER_DOCUMENTS_BYTES.get(1)));
        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);
    }

    @Test
    void shouldMakeCorrectCallsToCreateAndSendDocuments() {
        underTest.send(MAIN_DOCUMENT_REFERENCE, RECIPIENTS, CASE_ID, CASE_NUMBER, Language.ENGLISH);

        verify(downloadService).downloadDocument(MAIN_DOCUMENT_REFERENCE.getBinaryUrl());
        verify(uploadService).uploadPDF(MAIN_DOCUMENT_BYTES, MAIN_DOCUMENT_FILE_NAME);
        verify(coverDocumentsService).createCoverDocuments(CASE_NUMBER, CASE_ID, RECIPIENTS.get(0), Language.ENGLISH);
        verify(coverDocumentsService).createCoverDocuments(CASE_NUMBER, CASE_ID, RECIPIENTS.get(1), Language.ENGLISH);
        verify(uploadService).uploadPDF(COVER_DOCUMENTS_BYTES.get(0), COVERSHEET_NAME);
        verify(uploadService).uploadPDF(COVER_DOCUMENTS_BYTES.get(1), COVERSHEET_NAME);
        verify(sendLetterApi, times(2))
            .sendLetter(eq(SERVICE_AUTH_TOKEN), letterWithPdfsRequestArgumentCaptor.capture());

        List<LetterWithPdfsRequest> letterWithPdfsRequestValues = letterWithPdfsRequestArgumentCaptor.getAllValues();
        Map<String, Object> requestData = Map.of(
            "caseId", CASE_ID,
            "documentName", MAIN_DOCUMENT_FILE_NAME
        );
        assertThat(letterWithPdfsRequestValues.get(0).getDocuments())
            .isEqualTo(List.of(COVER_DOCUMENTS_ENCODED.get(0), MAIN_DOCUMENT_ENCODED));
        assertThat(letterWithPdfsRequestValues.get(0).getAdditionalData())
            .isEqualTo(requestData);
        assertThat(letterWithPdfsRequestValues.get(1).getDocuments())
            .isEqualTo(List.of(COVER_DOCUMENTS_ENCODED.get(1), MAIN_DOCUMENT_ENCODED));
        assertThat(letterWithPdfsRequestValues.get(1).getAdditionalData())
            .isEqualTo(requestData);
    }

    @Test
    void shouldReturnSentDocumentsData() {
        List<SentDocument> sentDocuments = underTest.send(
            MAIN_DOCUMENT_REFERENCE, RECIPIENTS, CASE_ID, CASE_NUMBER, Language.ENGLISH
        );

        String formattedDate = "1:01am, 1 January 0001";

        assertThat(sentDocuments.get(0)).isEqualTo(SentDocument.builder()
            .partyName(RECIPIENTS.get(0).getFullName())
            .document(buildFromDocument(UPLOADED_MAIN_DOCUMENT))
            .coversheet(buildFromDocument(COVERSHEETS.get(0)))
            .sentAt(formattedDate)
            .letterId(LETTERS_IDS.get(0).toString())
            .build());

        assertThat(sentDocuments.get(1)).isEqualTo(SentDocument.builder()
            .partyName(RECIPIENTS.get(1).getFullName())
            .document(buildFromDocument(UPLOADED_MAIN_DOCUMENT))
            .coversheet(buildFromDocument(COVERSHEETS.get(1)))
            .sentAt(formattedDate)
            .letterId(LETTERS_IDS.get(1).toString())
            .build());
    }
}
