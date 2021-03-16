package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisCoverDocumentsService;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;
import uk.gov.service.notify.NotificationClient;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_ADMIN_EMAIL;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_CTSC_EMAIL;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_INBOX;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createOrders;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParametersMap;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParametersMapForRepresentatives;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testAddress;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinary;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;
import static uk.gov.hmcts.reform.fpl.utils.matchers.JsonMatcher.eqJson;

@WebMvcTest(GeneratedOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class GeneratedOrderControllerSubmittedTest extends AbstractCallbackTest {
    private static final Long CASE_ID = 1614860986487554L;
    private static final String NOTIFICATION_REFERENCE = "localhost/" + CASE_ID;
    private static final String FAMILY_MAN_CASE_NUMBER = "FMN1";
    private static final UUID LETTER_1_ID = randomUUID();
    private static final UUID LETTER_2_ID = randomUUID();
    private static final Document ORDER_DOCUMENT = testDocument();
    private static final Document COVERSHEET_REPRESENTATIVE = testDocument();
    private static final Document COVERSHEET_RESPONDENT = testDocument();
    private static final byte[] ORDER_BINARY = testDocumentBinary();
    private static final byte[] COVERSHEET_REPRESENTATIVE_BINARY = testDocumentBinary();
    private static final byte[] COVERSHEET_RESPONDENT_BINARY = testDocumentBinary();
    private static final DocumentReference ORDER = testDocumentReference();
    private static final Element<Representative> REPRESENTATIVE_POST = element(Representative.builder()
        .fullName("First Representative")
        .servingPreferences(POST)
        .address(testAddress())
        .build());
    private static final Element<Representative> REPRESENTATIVE_EMAIL = element(Representative.builder()
        .fullName("Third Representative")
        .servingPreferences(EMAIL)
        .email("third@representatives.com")
        .build());
    private static final Element<Representative> REPRESENTATIVE_DIGITAL = element(Representative.builder()
        .fullName("Second Representative")
        .servingPreferences(DIGITAL_SERVICE)
        .email("second@representatives.com")
        .build());
    private static final Respondent RESPONDENT_NOT_REPRESENTED = Respondent.builder()
        .party(RespondentParty.builder()
            .firstName("Alex")
            .lastName("Jones")
            .address(testAddress())
            .build())
        .build();
    private static final Respondent RESPONDENT_WITHOUT_ADDRESS = Respondent.builder()
        .party(RespondentParty.builder()
            .firstName("Emma")
            .lastName("Jones")
            .build())
        .build();
    private static final Respondent RESPONDENT_REPRESENTED = Respondent.builder()
        .party(RespondentParty.builder()
            .firstName("George")
            .lastName("Jones")
            .address(testAddress())
            .build())
        .representedBy(wrapElements(REPRESENTATIVE_POST.getId(), REPRESENTATIVE_DIGITAL.getId()))
        .build();
    private static final CaseData CASE_DATA = CaseData.builder()
        .id(CASE_ID)
        .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
        .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
        .orderCollection(createOrders(ORDER))
        .respondents1(wrapElements(RESPONDENT_NOT_REPRESENTED, RESPONDENT_WITHOUT_ADDRESS, RESPONDENT_REPRESENTED))
        .representatives(List.of(REPRESENTATIVE_POST, REPRESENTATIVE_DIGITAL, REPRESENTATIVE_EMAIL))
        .build();

    @Captor
    private ArgumentCaptor<Map<String, Object>> caseDetails;

    @Captor
    private ArgumentCaptor<LetterWithPdfsRequest> printRequest;

    @MockBean
    private SendLetterApi sendLetterApi;

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @MockBean
    private DocmosisCoverDocumentsService documentService;

    GeneratedOrderControllerSubmittedTest() {
        super("create-order");
    }

    @BeforeEach
    void init() {
        givenFplService();
        given(uploadDocumentService.uploadPDF(ORDER_BINARY, ORDER.getFilename()))
            .willReturn(ORDER_DOCUMENT);
        given(uploadDocumentService.uploadPDF(COVERSHEET_REPRESENTATIVE_BINARY, "Coversheet.pdf"))
            .willReturn(COVERSHEET_REPRESENTATIVE);
        given(uploadDocumentService.uploadPDF(COVERSHEET_RESPONDENT_BINARY, "Coversheet.pdf"))
            .willReturn(COVERSHEET_RESPONDENT);
        given(documentDownloadService.downloadDocument(anyString()))
            .willReturn(ORDER_BINARY);
        given(documentService.createCoverDocuments(any(), any(), eq(REPRESENTATIVE_POST.getValue())))
            .willReturn(DocmosisDocument.builder().bytes(COVERSHEET_REPRESENTATIVE_BINARY).build());
        given(documentService.createCoverDocuments(any(), any(), eq(RESPONDENT_NOT_REPRESENTED.getParty())))
            .willReturn(DocmosisDocument.builder().bytes(COVERSHEET_RESPONDENT_BINARY).build());
        given(sendLetterApi.sendLetter(any(), any(LetterWithPdfsRequest.class)))
            .willReturn(new SendLetterResponse(LETTER_1_ID))
            .willReturn(new SendLetterResponse(LETTER_2_ID));
    }

    @Test
    void shouldNotifyRelevantPartiesWhenOrderIssued() throws Exception {

        postSubmittedEvent(CASE_DATA);

        verify(notificationClient).sendEmail(
            eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES),
            eq(REPRESENTATIVE_EMAIL.getValue().getEmail()),
            eqJson(getExpectedParametersMapForRepresentatives(BLANK_ORDER.getLabel(), true)),
            eq(NOTIFICATION_REFERENCE));

        verify(notificationClient).sendEmail(
            eq(ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES),
            eq(REPRESENTATIVE_DIGITAL.getValue().getEmail()),
            eqJson(getExpectedParametersMap(BLANK_ORDER.getLabel(), true)),
            eq(NOTIFICATION_REFERENCE));

        verify(notificationClient).sendEmail(
            eq(ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES),
            eq(LOCAL_AUTHORITY_1_INBOX),
            eqJson(getExpectedParametersMap(BLANK_ORDER.getLabel(), true)),
            eq(NOTIFICATION_REFERENCE));

        verify(notificationClient).sendEmail(
            eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN),
            eq(DEFAULT_ADMIN_EMAIL),
            eqJson(getExpectedParametersMap(BLANK_ORDER.getLabel(), true)),
            eq(NOTIFICATION_REFERENCE));

        verify(sendLetterApi, times(2)).sendLetter(eq(SERVICE_AUTH_TOKEN), printRequest.capture());
        verify(coreCaseDataService).updateCase(eq(CASE_ID), this.caseDetails.capture());

        LetterWithPdfsRequest expectedPrintRequest1 = printRequest(CASE_ID, ORDER, COVERSHEET_REPRESENTATIVE_BINARY,
            ORDER_BINARY);

        LetterWithPdfsRequest expectedPrintRequest2 = printRequest(CASE_ID, ORDER, COVERSHEET_RESPONDENT_BINARY,
            ORDER_BINARY);

        assertThat(printRequest.getAllValues()).usingRecursiveComparison()
            .isEqualTo(List.of(expectedPrintRequest1, expectedPrintRequest2));

        final CaseData caseUpdate = getCase(this.caseDetails);

        SentDocument expectedDocumentSentToRepresentative = documentSent(REPRESENTATIVE_POST.getValue(),
            COVERSHEET_REPRESENTATIVE, ORDER_DOCUMENT, LETTER_1_ID);

        SentDocument expectedDocumentSentToRespondent = documentSent(RESPONDENT_NOT_REPRESENTED.getParty(),
            COVERSHEET_RESPONDENT, ORDER_DOCUMENT, LETTER_2_ID);

        assertThat(caseUpdate.getDocumentsSentToParties()).hasSize(2);

        assertThat(caseUpdate.getDocumentsSentToParties().get(0).getValue().getDocumentsSentToParty())
            .extracting(Element::getValue)
            .containsExactly(expectedDocumentSentToRepresentative);

        assertThat(caseUpdate.getDocumentsSentToParties().get(1).getValue().getDocumentsSentToParty())
            .extracting(Element::getValue)
            .containsExactly(expectedDocumentSentToRespondent);
    }

    @Test
    void shouldNotifyCtscAdminWhenOrderIssuedAndCtscEnabled() throws Exception {

        CaseData caseData = CASE_DATA.toBuilder().sendToCtsc("Yes").build();

        postSubmittedEvent(caseData);

        verify(notificationClient).sendEmail(
            eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN),
            eq(DEFAULT_CTSC_EMAIL),
            eqJson(getExpectedParametersMap(BLANK_ORDER.getLabel(), true)),
            eq(NOTIFICATION_REFERENCE));

        verify(notificationClient, never()).sendEmail(
            eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN),
            eq(DEFAULT_ADMIN_EMAIL),
            any(),
            any());
    }

    private LetterWithPdfsRequest printRequest(Long caseId, DocumentReference order, byte[]... binaries) {
        List<String> documents = Stream.of(binaries)
            .map(Base64.getEncoder()::encodeToString)
            .collect(toList());
        Map<String, Object> parameters = Map.of("caseId", caseId, "documentName", order.getFilename());
        return new LetterWithPdfsRequest(documents, "FPLA001", parameters);
    }

    private SentDocument documentSent(Recipient recipient, Document coversheet, Document document, UUID letterId) {
        return SentDocument.builder()
            .partyName(recipient.getFullName())
            .letterId(letterId.toString())
            .document(DocumentReference.buildFromDocument(document))
            .coversheet(DocumentReference.buildFromDocument(coversheet))
            .sentAt(formatLocalDateTimeBaseUsingFormat(now(), "h:mma, d MMMM yyyy"))
            .build();
    }

}
