package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testRepresentative;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {FixedTimeConfiguration.class})
class DocumentSenderServiceTest {

    private static final String SERVICE_AUTH_TOKEN = "Service token";
    private static final String AUTH_TOKEN = "User token";
    private static final String USER_ID = UUID.randomUUID().toString();
    private static final LocalDateTime DATE = LocalDateTime.of(2019, 1, 1, 12, 0, 0);
    private static final String FORMATTED_DATE = "12:00pm, 1 January 2019";
    private static final String FAMILY_CASE_NUMBER = "familyCaseNumber";
    private static final String COVERSHEET_NAME = "Coversheet.pdf";
    private static final Long CASE_ID = 1L;
    private static final DocumentReference DOCUMENT_REFERENCE = testDocumentReference();
    private static final byte[] MAIN_DOCUMENT_BYTES = new byte[]{1, 2, 3, 4, 5};
    private static final List<Document> COVERSHEETS = List.of(testDocument(), testDocument());
    private static final List<byte[]> COVER_DOCUMENTS_BYTES = List.of(new byte[]{0}, new byte[]{1});
    private static final List<Representative> REPRESENTATIVES = List.of(testRepresentative(), testRepresentative());
    private static final List<UUID> LETTERS_IDS = List.of(UUID.randomUUID(), UUID.randomUUID());

    private DocumentSenderService documentSenderService;

    @Mock
    private Time time;

    @Mock
    private SendLetterApi sendLetterApi;

    @Mock
    private DocumentDownloadService documentDownloadService;

    @Mock
    private UploadDocumentService uploadDocumentService;

    @Mock
    private DocmosisCoverDocumentsService docmosisCoverDocumentsService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private RequestData requestData;

    @Captor
    private ArgumentCaptor<LetterWithPdfsRequest> letterWithPdfsRequestArgumentCaptor;

    @BeforeEach
    void setup() {
        given(time.now()).willReturn(DATE);
        given(requestData.authorisation()).willReturn(AUTH_TOKEN);
        given(requestData.userId()).willReturn(USER_ID);
        given(uploadDocumentService.uploadPDF(COVER_DOCUMENTS_BYTES.get(0), COVERSHEET_NAME))
            .willReturn(COVERSHEETS.get(0));
        given(uploadDocumentService.uploadPDF(COVER_DOCUMENTS_BYTES.get(1), COVERSHEET_NAME))
            .willReturn(COVERSHEETS.get(1));
        given(sendLetterApi.sendLetter(anyString(), any(LetterWithPdfsRequest.class)))
            .willReturn(new SendLetterResponse(LETTERS_IDS.get(0)))
            .willReturn(new SendLetterResponse(LETTERS_IDS.get(1)));
        given(documentDownloadService.downloadDocument(anyString())).willReturn(MAIN_DOCUMENT_BYTES);
        given(docmosisCoverDocumentsService.createCoverDocuments(FAMILY_CASE_NUMBER, CASE_ID, REPRESENTATIVES.get(0)))
            .willReturn(testDocmosisDocument(COVER_DOCUMENTS_BYTES.get(0)));
        given(docmosisCoverDocumentsService.createCoverDocuments(FAMILY_CASE_NUMBER, CASE_ID, REPRESENTATIVES.get(1)))
            .willReturn(testDocmosisDocument(COVER_DOCUMENTS_BYTES.get(1)));
        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);

        documentSenderService = new DocumentSenderService(
            time,
            sendLetterApi,
            documentDownloadService,
            docmosisCoverDocumentsService,
            authTokenGenerator,
            uploadDocumentService);
    }

    @Test
    void shouldMakeCorrectCallsToCreateAndSendDocuments() {
        DocumentReference documentToBeSent = testDocumentReference();
        String familyCaseNumber = "familyCaseNumber";

        documentSenderService.send(documentToBeSent, REPRESENTATIVES, CASE_ID, familyCaseNumber);

        verify(documentDownloadService).downloadDocument(documentToBeSent.getBinaryUrl());
        verify(docmosisCoverDocumentsService).createCoverDocuments(familyCaseNumber, CASE_ID, REPRESENTATIVES.get(0));
        verify(docmosisCoverDocumentsService).createCoverDocuments(familyCaseNumber, CASE_ID, REPRESENTATIVES.get(1));
        verify(uploadDocumentService).uploadPDF(COVER_DOCUMENTS_BYTES.get(0), COVERSHEET_NAME);
        verify(uploadDocumentService).uploadPDF(COVER_DOCUMENTS_BYTES.get(1), COVERSHEET_NAME);
        verify(sendLetterApi, times(2))
            .sendLetter(eq(SERVICE_AUTH_TOKEN), letterWithPdfsRequestArgumentCaptor.capture());

        List<LetterWithPdfsRequest> letterWithPdfsRequestValues = letterWithPdfsRequestArgumentCaptor.getAllValues();
        assertThat(letterWithPdfsRequestValues.get(0).getDocuments())
            .isEqualTo(List.of(COVER_DOCUMENTS_BYTES.get(0), MAIN_DOCUMENT_BYTES));
        assertThat(letterWithPdfsRequestValues.get(0).getAdditionalData())
            .isEqualTo(Map.of("caseId", CASE_ID, "documentName", documentToBeSent.getFilename()));
        assertThat(letterWithPdfsRequestValues.get(1).getDocuments())
            .isEqualTo(List.of(COVER_DOCUMENTS_BYTES.get(1),
                MAIN_DOCUMENT_BYTES));
        assertThat(letterWithPdfsRequestValues.get(1).getAdditionalData())
            .isEqualTo(Map.of("caseId", CASE_ID, "documentName", documentToBeSent.getFilename()));
    }

    @Test
    void shouldReturnSentDocumentsData() {
        List<SentDocument> sentDocuments = documentSenderService.send(DOCUMENT_REFERENCE, REPRESENTATIVES, CASE_ID,
            FAMILY_CASE_NUMBER);

        assertThat(sentDocuments.get(0)).isEqualTo(SentDocument.builder()
            .partyName(REPRESENTATIVES.get(0).getFullName())
            .document(DOCUMENT_REFERENCE)
            .coversheet(buildFromDocument(COVERSHEETS.get(0)))
            .sentAt(FORMATTED_DATE)
            .letterId(LETTERS_IDS.get(0).toString())
            .build());

        assertThat(sentDocuments.get(1)).isEqualTo(SentDocument.builder()
            .partyName(REPRESENTATIVES.get(1).getFullName())
            .document(DOCUMENT_REFERENCE)
            .coversheet(buildFromDocument(COVERSHEETS.get(1)))
            .sentAt(FORMATTED_DATE)
            .letterId(LETTERS_IDS.get(1).toString())
            .build());
    }
}
