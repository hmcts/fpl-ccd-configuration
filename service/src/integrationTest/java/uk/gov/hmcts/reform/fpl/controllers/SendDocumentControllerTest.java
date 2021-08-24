package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.SentDocuments;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisCoverDocumentsService;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinaries;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testRepresentative;

@WebMvcTest(SendDocumentController.class)
@OverrideAutoConfiguration(enabled = true)
class SendDocumentControllerTest extends AbstractCallbackTest {

    private static final String FAMILY_MAN_NO = RandomStringUtils.randomAlphabetic(10);
    private static final Document COVERSHEET_DOCUMENT = testDocument();
    private static final Document MAIN_DOCUMENT = testDocument();
    private static final byte[] COVERSHEET_BINARIES = testDocumentBinaries();
    private static final byte[] MAIN_DOCUMENT_BINARIES = testDocumentBinaries();
    private static final UUID LETTER_ID = UUID.randomUUID();

    @MockBean
    private DocmosisCoverDocumentsService docmosisCoverDocumentsService;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @MockBean
    private SendLetterApi sendLetterApi;

    SendDocumentControllerTest() {
        super("send-document");
    }

    @BeforeEach
    void setup() {
        givenFplService();
        given(documentDownloadService.downloadDocument(anyString())).willReturn(MAIN_DOCUMENT_BINARIES);
        given(docmosisCoverDocumentsService.createCoverDocuments(any(), any(), any(), Language.ENGLISH))
            .willReturn(testDocmosisDocument(COVERSHEET_BINARIES));
        given(uploadDocumentService.uploadPDF(eq(COVERSHEET_BINARIES), any())).willReturn(COVERSHEET_DOCUMENT);
        given(uploadDocumentService.uploadPDF(eq(MAIN_DOCUMENT_BINARIES), any())).willReturn(MAIN_DOCUMENT);
        given(sendLetterApi.sendLetter(anyString(), any(LetterWithPdfsRequest.class)))
            .willReturn(new SendLetterResponse(LETTER_ID));
    }

    @Test
    void shouldSendDocumentToRepresentativesWithPostServingPreferences() {
        Representative representative1 = testRepresentative(POST);
        Representative representative2 = testRepresentative(EMAIL);
        Representative representative3 = testRepresentative(DIGITAL_SERVICE);

        DocumentReference documentToBeSent = testDocumentReference();
        DocumentReference coversheet = buildFromDocument(COVERSHEET_DOCUMENT);
        DocumentReference mainDocument = buildFromDocument(MAIN_DOCUMENT);

        CaseDetails caseDetails = buildCaseData(documentToBeSent, representative1, representative2, representative3);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

        verify(documentDownloadService).downloadDocument(documentToBeSent.getBinaryUrl());
        verify(sendLetterApi).sendLetter(anyString(), any(LetterWithPdfsRequest.class));
        verify(uploadDocumentService).uploadPDF(COVERSHEET_BINARIES, "Coversheet.pdf");
        verify(docmosisCoverDocumentsService).createCoverDocuments(FAMILY_MAN_NO, caseDetails.getId(), representative1,
            Language.ENGLISH);

        List<SentDocuments> documentsSentToParties = unwrapElements(mapper.convertValue(
            callbackResponse.getData().get("documentsSentToParties"), new TypeReference<>() {
            }));

        assertThat(documentsSentToParties).hasSize(1);
        assertThat(documentsSentToParties.get(0).getPartyName()).isEqualTo(representative1.getFullName());
        assertThat(unwrapElements(documentsSentToParties.get(0).getDocumentsSentToParty()))
            .containsExactly(SentDocument.builder()
                .partyName(representative1.getFullName())
                .document(mainDocument)
                .coversheet(coversheet)
                .sentAt(formatLocalDateTimeBaseUsingFormat(now(), TIME_DATE))
                .letterId(LETTER_ID.toString())
                .build());
    }

    @Test
    void shouldNotSendDocumentWhenPrintingIsDisabled() {
        DocumentReference documentToBeSend = testDocumentReference();
        CaseDetails caseDetails = buildCaseData(documentToBeSend);

        postAboutToSubmitEvent(caseDetails);

        verifyNoDocumentSent();
    }

    @Test
    void shouldNotSendDocumentWhenNoRepresentativesServedByPost() {
        DocumentReference documentToBeSend = testDocumentReference();
        CaseDetails caseDetails = buildCaseData(
            documentToBeSend,
            testRepresentative(EMAIL),
            testRepresentative(DIGITAL_SERVICE));

        postAboutToSubmitEvent(caseDetails);

        verifyNoDocumentSent();
    }

    private void verifyNoDocumentSent() {
        verify(docmosisCoverDocumentsService, never()).createCoverDocuments(any(), any(), any(), Language.ENGLISH);
        verify(documentDownloadService, never()).downloadDocument(any());
        verify(uploadDocumentService, never()).uploadPDF(any(), any());
        verify(sendLetterApi, never()).sendLetter(any(), any(LetterWithPdfsRequest.class));
    }

    private static CaseDetails buildCaseData(DocumentReference documentReference, Representative... representatives) {
        return CaseDetails.builder()
            .id(RandomUtils.nextLong())
            .data(Map.of(
                "familyManCaseNumber", FAMILY_MAN_NO,
                "documentToBeSent", documentReference,
                "representatives", wrapElements(representatives)))
            .build();
    }
}
