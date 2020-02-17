package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {FixedTimeConfiguration.class,  DateFormatterService.class})
class DocumentSenderServiceTest {

    private static final String SERVICE_AUTH_TOKEN = "Bearer service token";
    private static final String FORMATTED_DATE = "12:00pm, 1 January 2019";
    private static final String FAMILY_CASE_NUMBER = "familyCaseNumber";
    private static final Long CASE_ID = 1L;
    private static final DocumentReference DOCUMENT_REFERENCE = TestDataHelper.testDocument();
    private static final byte[] MAIN_DOCUMENT_BYTES = new byte[] {1, 2, 3, 4, 5};
    private static final List<byte[]> COVER_DOCUMENTS_BYTES = List.of(new byte[] {0}, new byte[] {1});
    private static final List<Representative> REPRESENTATIVES = List.of(
        Representative.builder().fullName("John Doe").build(),
        Representative.builder().fullName("Foo Bar").build());

    private DocumentSenderService documentSenderService;

    @Autowired
    private Time time;

    @Mock
    private DateFormatterService dateFormatterService;

    @Mock
    private SendLetterApi sendLetterApi;

    @Mock
    private DocumentDownloadService documentDownloadService;

    @Mock
    private DocmosisCoverDocumentsService docmosisCoverDocumentsService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Captor
    private ArgumentCaptor<LetterWithPdfsRequest> letterWithPdfsRequestArgumentCaptor;

    @BeforeEach
    void setup() {
        given(sendLetterApi.sendLetter(anyString(),
            any(LetterWithPdfsRequest.class))).willReturn(new SendLetterResponse(UUID.randomUUID()));
        given(documentDownloadService.downloadDocument(anyString())).willReturn(MAIN_DOCUMENT_BYTES);
        given(docmosisCoverDocumentsService.createCoverDocuments(FAMILY_CASE_NUMBER,
            CASE_ID,
            REPRESENTATIVES.get(0))).willReturn(DocmosisDocument.builder().bytes(COVER_DOCUMENTS_BYTES.get(0)).build());
        given(docmosisCoverDocumentsService.createCoverDocuments(FAMILY_CASE_NUMBER,
            CASE_ID,
            REPRESENTATIVES.get(1))).willReturn(DocmosisDocument.builder().bytes(COVER_DOCUMENTS_BYTES.get(1)).build());
        given(dateFormatterService.formatLocalDateTimeBaseUsingFormat(any(), anyString())).willReturn(FORMATTED_DATE);
        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);

        documentSenderService = new DocumentSenderService(time,
            dateFormatterService,
            sendLetterApi,
            documentDownloadService,
            docmosisCoverDocumentsService,
            authTokenGenerator);
    }

    @Test
    void shouldMakeCorrectCallsToCreateAndSendDocuments() {
        DocumentReference documentToBeSent = TestDataHelper.testDocument();
        String familyCaseNumber = "familyCaseNumber";

        documentSenderService.send(documentToBeSent, REPRESENTATIVES, CASE_ID, familyCaseNumber);

        verify(documentDownloadService).downloadDocument(documentToBeSent.getBinaryUrl());
        verify(docmosisCoverDocumentsService).createCoverDocuments(familyCaseNumber, CASE_ID, REPRESENTATIVES.get(0));
        verify(docmosisCoverDocumentsService).createCoverDocuments(familyCaseNumber, CASE_ID, REPRESENTATIVES.get(1));
        verify(sendLetterApi, times(2))
            .sendLetter(eq(SERVICE_AUTH_TOKEN), letterWithPdfsRequestArgumentCaptor.capture());

        List<LetterWithPdfsRequest> letterWithPdfsRequestValues = letterWithPdfsRequestArgumentCaptor.getAllValues();
        assertThat(letterWithPdfsRequestValues.get(0).getDocuments())
            .isEqualTo(List.of(COVER_DOCUMENTS_BYTES.get(0), MAIN_DOCUMENT_BYTES));
        assertThat(letterWithPdfsRequestValues.get(1).getDocuments())
            .isEqualTo(List.of(COVER_DOCUMENTS_BYTES.get(1),
            MAIN_DOCUMENT_BYTES));
    }

    @Test
    void shouldReturnSentDocumentsData() {
        List<SentDocument> sentDocuments = documentSenderService.send(DOCUMENT_REFERENCE, REPRESENTATIVES, 1L,
            FAMILY_CASE_NUMBER);

        verify(dateFormatterService, times(2)).formatLocalDateTimeBaseUsingFormat(time.now(),
            "h:mma, d MMMM yyyy");

        assertThat(sentDocuments.get(0)).isEqualTo(SentDocument.builder()
            .partyName(REPRESENTATIVES.get(0).getFullName())
            .document(DOCUMENT_REFERENCE)
            .sentAt(FORMATTED_DATE).build());
        assertThat(sentDocuments.get(1)).isEqualTo(SentDocument.builder()
            .partyName(REPRESENTATIVES.get(1).getFullName())
            .document(DOCUMENT_REFERENCE)
            .sentAt(FORMATTED_DATE).build());
    }
}
