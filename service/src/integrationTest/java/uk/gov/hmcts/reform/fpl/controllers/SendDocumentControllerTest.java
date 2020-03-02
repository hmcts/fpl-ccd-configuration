package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.launchdarkly.client.LDClient;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.DocumentsSentToParty;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.DocmosisCoverDocumentsService;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinaries;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testRepresentative;

@ActiveProfiles("integration-test")
@WebMvcTest(SendDocumentController.class)
@OverrideAutoConfiguration(enabled = true)
class SendDocumentControllerTest extends AbstractControllerTest {

    private static final String SERVICE_AUTH_TOKEN = "Service token";
    private static final String FAMILY_MAN_NO = RandomStringUtils.randomAlphabetic(10);
    private static final Document COVERSHEET_DOCUMENT = testDocument();
    private static final byte[] COVERSHEET_BINARIES = testDocumentBinaries();
    private static final byte[] DOCUMENT_BINARIES = testDocumentBinaries();

    @MockBean
    private Time time;

    @MockBean
    private LDClient ldClient;

    @MockBean
    private DocmosisCoverDocumentsService docmosisCoverDocumentsService;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @MockBean
    private SendLetterApi sendLetterApi;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    SendDocumentControllerTest() {
        super("send-document");
    }

    @BeforeEach
    void setup() {
        given(time.now()).willReturn(LocalDateTime.parse("2020-01-05T12:10"));
        given(documentDownloadService.downloadDocument(anyString())).willReturn(DOCUMENT_BINARIES);
        given(docmosisCoverDocumentsService.createCoverDocuments(any(), any(), any()))
            .willReturn(testDocmosisDocument(COVERSHEET_BINARIES));
        given(uploadDocumentService.uploadPDF(any(), any(), any(), any())).willReturn(COVERSHEET_DOCUMENT);
        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);
    }

    @Test
    void shouldSendDocumentToRepresentativesWithPostServingPreferences() {
        givenPrintingEnabled(true);

        Representative representative1 = testRepresentative(POST);
        Representative representative2 = testRepresentative(EMAIL);
        Representative representative3 = testRepresentative(DIGITAL_SERVICE);

        DocumentReference documentToBeSent = testDocumentReference();
        DocumentReference coversheet = buildFromDocument(COVERSHEET_DOCUMENT);

        CaseDetails caseDetails = buildCaseData(documentToBeSent, representative1, representative2, representative3);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

        verify(documentDownloadService).downloadDocument(documentToBeSent.getBinaryUrl());
        verify(sendLetterApi).sendLetter(anyString(), any(LetterWithPdfsRequest.class));
        verify(uploadDocumentService).uploadPDF(userId, userAuthToken, COVERSHEET_BINARIES, "Coversheet.pdf");
        verify(docmosisCoverDocumentsService).createCoverDocuments(FAMILY_MAN_NO, caseDetails.getId(), representative1);

        List<DocumentsSentToParty> documentsSentToParties = unwrapElements(mapper.convertValue(
            callbackResponse.getData().get("documentsSentToParties"), new TypeReference<>() {
            }));

        assertThat(documentsSentToParties).hasSize(1);
        assertThat(documentsSentToParties.get(0).getPartyName()).isEqualTo(representative1.getFullName());
        assertThat(unwrapElements(documentsSentToParties.get(0).getDocumentsSentToParty()))
            .containsExactly(SentDocument.builder()
                .partyName(representative1.getFullName())
                .document(documentToBeSent)
                .coversheet(coversheet)
                .sentAt("12:10pm, 5 January 2020")
                .build());
    }

    @Test
    void shouldNotSendDocumentWhenPrintingIsDisabled() {
        givenPrintingEnabled(false);

        DocumentReference documentToBeSend = testDocumentReference();
        CaseDetails caseDetails = buildCaseData(documentToBeSend);

        postAboutToSubmitEvent(caseDetails);

        verify(docmosisCoverDocumentsService, never()).createCoverDocuments(any(), any(), any());
        verify(documentDownloadService, never()).downloadDocument(any());
        verify(uploadDocumentService, never()).uploadPDF(any(), any(), any(), any());
        verify(sendLetterApi, never()).sendLetter(any(), any(LetterWithPdfsRequest.class));
    }

    private void givenPrintingEnabled(boolean enabled) {
        given(ldClient.boolVariation(eq("xerox-printing"), any(), anyBoolean())).willReturn(enabled);
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
