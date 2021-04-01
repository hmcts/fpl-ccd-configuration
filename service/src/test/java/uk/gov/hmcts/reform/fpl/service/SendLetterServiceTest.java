package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisCoverDocumentsService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.util.Base64;
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
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinaries;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testRepresentative;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {FixedTimeConfiguration.class, SendLetterService.class})
class SendLetterServiceTest {

    private static final String SERVICE_AUTH_TOKEN = "Service token";
    private static final String FAMILY_CASE_NUMBER = "familyCaseNumber";
    private static final String COVERSHEET_NAME = "Coversheet.pdf";
    private static final Long CASE_ID = 1L;
    private static final DocumentReference MAIN_DOCUMENT_REFERENCE = testDocumentReference();
    private static final byte[] MAIN_DOCUMENT_BYTES = testDocumentBinaries();
    private static final String MAIN_DOCUMENT_ENCODED = Base64.getEncoder().encodeToString(MAIN_DOCUMENT_BYTES);
    private static final Document UPLOADED_MAIN_DOCUMENT = testDocument();
    private static final List<byte[]> COVER_DOCUMENTS_BYTES = List.of(new byte[]{0}, new byte[]{1});
    private static final List<String> COVER_DOCUMENTS_ENCODED = List.of(
        Base64.getEncoder().encodeToString(COVER_DOCUMENTS_BYTES.get(0)),
        Base64.getEncoder().encodeToString(COVER_DOCUMENTS_BYTES.get(1))
    );
    private static final List<Document> COVERSHEETS = List.of(testDocument(), testDocument());
    private static final List<Recipient> RECIPIENTS = List.of(testRepresentative(), testRepresentative());
    private static final List<UUID> LETTERS_IDS = List.of(UUID.randomUUID(), UUID.randomUUID());

    @Autowired
    private SendLetterService underTest;

    @Autowired
    private Time time;

    @MockBean
    private SendLetterApi sendLetterApi;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @MockBean
    private DocmosisCoverDocumentsService docmosisCoverDocumentsService;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @Captor
    private ArgumentCaptor<LetterWithPdfsRequest> letterWithPdfsRequestArgumentCaptor;

    @BeforeEach
    void setup() {
        given(uploadDocumentService.uploadPDF(MAIN_DOCUMENT_BYTES, MAIN_DOCUMENT_REFERENCE.getFilename()))
            .willReturn(UPLOADED_MAIN_DOCUMENT);
        given(uploadDocumentService.uploadPDF(COVER_DOCUMENTS_BYTES.get(0), COVERSHEET_NAME))
            .willReturn(COVERSHEETS.get(0));
        given(uploadDocumentService.uploadPDF(COVER_DOCUMENTS_BYTES.get(1), COVERSHEET_NAME))
            .willReturn(COVERSHEETS.get(1));
        given(sendLetterApi.sendLetter(anyString(), any(LetterWithPdfsRequest.class)))
            .willReturn(new SendLetterResponse(LETTERS_IDS.get(0)))
            .willReturn(new SendLetterResponse(LETTERS_IDS.get(1)));
        given(documentDownloadService.downloadDocument(MAIN_DOCUMENT_REFERENCE.getBinaryUrl()))
            .willReturn(MAIN_DOCUMENT_BYTES);
        given(docmosisCoverDocumentsService.createCoverDocuments(FAMILY_CASE_NUMBER, CASE_ID, RECIPIENTS.get(0)))
            .willReturn(testDocmosisDocument(COVER_DOCUMENTS_BYTES.get(0)));
        given(docmosisCoverDocumentsService.createCoverDocuments(FAMILY_CASE_NUMBER, CASE_ID, RECIPIENTS.get(1)))
            .willReturn(testDocmosisDocument(COVER_DOCUMENTS_BYTES.get(1)));
        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);
    }

    @Test
    void shouldMakeCorrectCallsToCreateAndSendDocuments() {
        String familyCaseNumber = "familyCaseNumber";

        underTest.send(MAIN_DOCUMENT_REFERENCE, RECIPIENTS, CASE_ID, familyCaseNumber);

        verify(documentDownloadService).downloadDocument(MAIN_DOCUMENT_REFERENCE.getBinaryUrl());
        verify(uploadDocumentService).uploadPDF(MAIN_DOCUMENT_BYTES, MAIN_DOCUMENT_REFERENCE.getFilename());
        verify(docmosisCoverDocumentsService).createCoverDocuments(familyCaseNumber, CASE_ID, RECIPIENTS.get(0));
        verify(docmosisCoverDocumentsService).createCoverDocuments(familyCaseNumber, CASE_ID, RECIPIENTS.get(1));
        verify(uploadDocumentService).uploadPDF(COVER_DOCUMENTS_BYTES.get(0), COVERSHEET_NAME);
        verify(uploadDocumentService).uploadPDF(COVER_DOCUMENTS_BYTES.get(1), COVERSHEET_NAME);
        verify(sendLetterApi, times(2))
            .sendLetter(eq(SERVICE_AUTH_TOKEN), letterWithPdfsRequestArgumentCaptor.capture());

        List<LetterWithPdfsRequest> letterWithPdfsRequestValues = letterWithPdfsRequestArgumentCaptor.getAllValues();
        assertThat(letterWithPdfsRequestValues.get(0).getDocuments())
            .isEqualTo(List.of(COVER_DOCUMENTS_ENCODED.get(0), MAIN_DOCUMENT_ENCODED));
        assertThat(letterWithPdfsRequestValues.get(0).getAdditionalData())
            .isEqualTo(Map.of("caseId", CASE_ID, "documentName", MAIN_DOCUMENT_REFERENCE.getFilename()));
        assertThat(letterWithPdfsRequestValues.get(1).getDocuments())
            .isEqualTo(List.of(COVER_DOCUMENTS_ENCODED.get(1), MAIN_DOCUMENT_ENCODED));
        assertThat(letterWithPdfsRequestValues.get(1).getAdditionalData())
            .isEqualTo(Map.of("caseId", CASE_ID, "documentName", MAIN_DOCUMENT_REFERENCE.getFilename()));
    }

    @Test
    void shouldReturnSentDocumentsData() {
        List<SentDocument> sentDocuments = underTest.send(MAIN_DOCUMENT_REFERENCE, RECIPIENTS, CASE_ID,
            FAMILY_CASE_NUMBER);

        String formattedDate = DateFormatterHelper.formatLocalDateTimeBaseUsingFormat(time.now(), "h:mma, d MMMM yyyy");

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
