package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.SentDocuments;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.document.SealType;
import uk.gov.hmcts.reform.fpl.model.event.UploadTranslationsEventData;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ccd.CCDConcurrencyHelper;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisCoverDocumentsService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocumentConversionService;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_INBOX;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ITEM_TRANSLATED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.controllers.helper.UploadTranslationsControllerTestHelper.CASE_DATA_WITH_ALL_ORDERS;
import static uk.gov.hmcts.reform.fpl.controllers.helper.UploadTranslationsControllerTestHelper.CONVERTED_DOC_BYTES;
import static uk.gov.hmcts.reform.fpl.controllers.helper.UploadTranslationsControllerTestHelper.RENDERED_DYNAMIC_LIST;
import static uk.gov.hmcts.reform.fpl.controllers.helper.UploadTranslationsControllerTestHelper.SEALED_DOC_BYTES;
import static uk.gov.hmcts.reform.fpl.controllers.helper.UploadTranslationsControllerTestHelper.TEST_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.controllers.helper.UploadTranslationsControllerTestHelper.TRANSLATED_DOC_BYTES;
import static uk.gov.hmcts.reform.fpl.controllers.helper.UploadTranslationsControllerTestHelper.UPLOADED_TRANSFORMED_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.controllers.helper.UploadTranslationsControllerTestHelper.UUID_1;
import static uk.gov.hmcts.reform.fpl.controllers.helper.UploadTranslationsControllerTestHelper.UUID_3;
import static uk.gov.hmcts.reform.fpl.controllers.helper.UploadTranslationsControllerTestHelper.UUID_4;
import static uk.gov.hmcts.reform.fpl.controllers.helper.UploadTranslationsControllerTestHelper.dlElement;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.ENGLISH_TO_WELSH;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.configuration.Language.ENGLISH;
import static uk.gov.hmcts.reform.fpl.model.configuration.Language.WELSH;
import static uk.gov.hmcts.reform.fpl.testingsupport.IntegrationTestConstants.COVERSHEET_PDF;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElementsWithUUIDs;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.documentSent;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.printRequest;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testAddress;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinaries;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(UploadTranslationsController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadTranslationsSubmittedControllerTest extends AbstractCallbackTest {

    private static final Long CASE_ID = 1614860986487554L;
    private static final String FAMILY_MAN_CASE_NUMBER = "FMN1";
    private static final UUID LETTER_1_ID = randomUUID();
    private static final UUID LETTER_2_ID = randomUUID();
    private static final UUID LETTER_3_ID = randomUUID();
    private static final UUID LETTER_4_ID = randomUUID();
    private static final Document ORDER_DOCUMENT = testDocument();
    private static final Document ORDER_DOCUMENT_ORIGINAL = testDocument();
    private static final Document COVERSHEET_REPRESENTATIVE = testDocument();
    private static final Document COVERSHEET_REPRESENTATIVE_ENGLISH = testDocument();
    private static final Document COVERSHEET_RESPONDENT = testDocument();
    private static final Document COVERSHEET_RESPONDENT_ENGLISH = testDocument();
    private static final byte[] ORDER_BINARY = testDocumentBinaries();
    private static final byte[] COVERSHEET_REPRESENTATIVE_BINARY = testDocumentBinaries();
    private static final byte[] COVERSHEET_REPRESENTATIVE_BINARY_ENGLISH = testDocumentBinaries();
    private static final byte[] COVERSHEET_RESPONDENT_BINARY = testDocumentBinaries();
    private static final byte[] COVERSHEET_RESPONDENT_BINARY_ENGLISH = testDocumentBinaries();
    private static final DocumentReference TRANSLATED_ORDER = testDocumentReference();
    private static final DocumentReference ORIGINAL_ORDER = testDocumentReference();
    private static final String ORDER_TYPE = "Care order (C32A)";
    private static final Map<String, Object> NOTIFICATION_PARAMETERS = getExpectedParametersMap(ORDER_TYPE);
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

    private static final Court court = Court.builder().name("Family Court").build();

    @Captor
    private ArgumentCaptor<Map<String, Object>> caseDataDelta;

    @Captor
    private ArgumentCaptor<LetterWithPdfsRequest> printRequest;

    @Captor
    private ArgumentCaptor<StartEventResponse> startEventResponseArgumentCaptor;

    @MockBean
    private SendLetterApi sendLetterApi;

    @MockBean
    private DocumentSealingService documentSealingService;

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private CCDConcurrencyHelper concurrencyHelper;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @MockBean
    private DocumentConversionService documentConversionService;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @MockBean
    private DocmosisCoverDocumentsService documentService;

    UploadTranslationsSubmittedControllerTest() {
        super("upload-translations");
    }

    @BeforeEach
    void mocks() {
        givenFplService();
        when(documentConversionService.convertToPdf(any()))
            .thenAnswer(returnsFirstArg());
        when(documentConversionService.convertToPdfBytes(TRANSLATED_ORDER))
            .thenReturn(ORDER_BINARY);
        when(documentConversionService.convertToPdfBytes(ORIGINAL_ORDER))
            .thenReturn(ORDER_BINARY);
        when(documentDownloadService.downloadDocument(anyString()))
            .thenReturn(ORDER_BINARY);
        when(uploadDocumentService.uploadPDF(ORDER_BINARY, TRANSLATED_ORDER.getFilename() + ".pdf"))
            .thenReturn(ORDER_DOCUMENT);
        when(uploadDocumentService.uploadPDF(ORDER_BINARY, ORIGINAL_ORDER.getFilename() + ".pdf"))
            .thenReturn(ORDER_DOCUMENT_ORIGINAL);

        when(documentService.createCoverDocuments(any(), any(), eq(REPRESENTATIVE_POST.getValue()), eq(WELSH)))
            .thenReturn(DocmosisDocument.builder().bytes(COVERSHEET_REPRESENTATIVE_BINARY).build());
        when(uploadDocumentService.uploadPDF(COVERSHEET_REPRESENTATIVE_BINARY, COVERSHEET_PDF))
            .thenReturn(COVERSHEET_REPRESENTATIVE);

        when(documentService.createCoverDocuments(any(), any(), eq(REPRESENTATIVE_POST.getValue()), eq(ENGLISH)))
            .thenReturn(DocmosisDocument.builder().bytes(COVERSHEET_REPRESENTATIVE_BINARY_ENGLISH).build());
        when(uploadDocumentService.uploadPDF(COVERSHEET_REPRESENTATIVE_BINARY_ENGLISH, COVERSHEET_PDF))
            .thenReturn(COVERSHEET_REPRESENTATIVE_ENGLISH);

        when(documentService.createCoverDocuments(any(), any(), eq(RESPONDENT_NOT_REPRESENTED.getParty()), eq(WELSH)))
            .thenReturn(DocmosisDocument.builder().bytes(COVERSHEET_RESPONDENT_BINARY).build());
        when(uploadDocumentService.uploadPDF(COVERSHEET_RESPONDENT_BINARY, COVERSHEET_PDF))
            .thenReturn(COVERSHEET_RESPONDENT);

        when(documentService.createCoverDocuments(any(), any(), eq(RESPONDENT_NOT_REPRESENTED.getParty()), eq(ENGLISH)))
            .thenReturn(DocmosisDocument.builder().bytes(COVERSHEET_RESPONDENT_BINARY_ENGLISH).build());
        when(uploadDocumentService.uploadPDF(COVERSHEET_RESPONDENT_BINARY_ENGLISH, COVERSHEET_PDF))
            .thenReturn(COVERSHEET_RESPONDENT_ENGLISH);

        when(sendLetterApi.sendLetter(any(), any(LetterWithPdfsRequest.class)))
            .thenReturn(
                new SendLetterResponse(LETTER_1_ID),
                new SendLetterResponse(LETTER_2_ID),
                new SendLetterResponse(LETTER_3_ID),
                new SendLetterResponse(LETTER_4_ID)
            );

        when(documentDownloadService.downloadDocument(TEST_DOCUMENT.getBinaryUrl())).thenReturn(TRANSLATED_DOC_BYTES);
        when(documentConversionService.convertToPdf(TRANSLATED_DOC_BYTES, TEST_DOCUMENT.getFilename())).thenReturn(
            CONVERTED_DOC_BYTES);
        when(documentSealingService.sealDocument(CONVERTED_DOC_BYTES, court, SealType.BILINGUAL))
            .thenReturn(SEALED_DOC_BYTES);
        when(uploadDocumentService.uploadDocument(SEALED_DOC_BYTES,
            "noticeo_c6-Welsh.pdf",
            RenderFormat.PDF.getMediaType()))
            .thenReturn(UPLOADED_TRANSFORMED_DOCUMENT);
    }

    @Test
    void shouldSendTranslatedNotificationToLocalAuthorityWhenTranslatedOrder() throws NotificationClientException {
        CaseData caseData = caseData();
        when(concurrencyHelper.startEvent(any(), any(String.class))).thenAnswer(i -> StartEventResponse.builder()
            .caseDetails(asCaseDetails(caseData))
            .eventId(i.getArgument(1))
            .token("token")
            .build());

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(asCaseDetails(caseData))
            .build();

        postSubmittedEvent(request);

        verify(notificationClient, timeout(ASYNC_METHOD_CALL_TIMEOUT)).sendEmail(
            ITEM_TRANSLATED_NOTIFICATION_TEMPLATE, LOCAL_AUTHORITY_1_INBOX,
            NOTIFICATION_PARAMETERS, notificationReference(CASE_ID)
        );
    }

    @Test
    void shouldSendOrdersByPostWhenOrderTranslated() {
        final CaseData caseData = caseData();
        when(concurrencyHelper.startEvent(any(), any(String.class))).thenAnswer(i -> StartEventResponse.builder()
            .caseDetails(asCaseDetails(caseData))
            .eventId(i.getArgument(1))
            .token("token")
            .build());

        postSubmittedEvent(caseData);

        verify(sendLetterApi, timeout(ASYNC_METHOD_CALL_TIMEOUT).times(4)).sendLetter(eq(SERVICE_AUTH_TOKEN),
            printRequest.capture());

        // 2 post submits, one sealing, one posting
        verify(concurrencyHelper, timeout(ASYNC_METHOD_CALL_TIMEOUT).times(2))
            .submitEvent(startEventResponseArgumentCaptor.capture(), eq(CASE_ID), caseDataDelta.capture());

        // only sending post submits for these events (posting, sealing)
        assertThat(startEventResponseArgumentCaptor.getAllValues().stream().map(StartEventResponse::getEventId))
            .containsOnly("internal-change-UPDATE_CASE", "internal-change-translations");

        assertThat(printRequest.getAllValues()).usingRecursiveComparison()
            .isEqualTo(List.of(
                printRequest(CASE_ID, ORIGINAL_ORDER, REPRESENTATIVE_POST.getValue(),
                    COVERSHEET_REPRESENTATIVE_BINARY_ENGLISH, ORDER_BINARY),
                printRequest(CASE_ID, ORIGINAL_ORDER, RESPONDENT_NOT_REPRESENTED.getParty(),
                    COVERSHEET_RESPONDENT_BINARY_ENGLISH, ORDER_BINARY),
                printRequest(CASE_ID, TRANSLATED_ORDER, REPRESENTATIVE_POST.getValue(),
                    COVERSHEET_REPRESENTATIVE_BINARY, ORDER_BINARY),
                printRequest(CASE_ID, TRANSLATED_ORDER, RESPONDENT_NOT_REPRESENTED.getParty(),
                    COVERSHEET_RESPONDENT_BINARY, ORDER_BINARY)
            ));

        List<Element<SentDocuments>> documentsSent = mapper.convertValue(
            caseDataDelta.getValue().get("documentsSentToParties"), new TypeReference<>() {
            }
        );

        assertThat(documentsSent.get(0).getValue().getDocumentsSentToParty())
            .extracting(Element::getValue)
            .containsExactly(
                documentSent(
                    REPRESENTATIVE_POST.getValue(),
                    COVERSHEET_REPRESENTATIVE_ENGLISH, ORDER_DOCUMENT_ORIGINAL, LETTER_1_ID, now()
                ),
                documentSent(
                    REPRESENTATIVE_POST.getValue(),
                    COVERSHEET_REPRESENTATIVE, ORDER_DOCUMENT, LETTER_3_ID, now()
                )
            );

        assertThat(documentsSent.get(1).getValue().getDocumentsSentToParty())
            .extracting(Element::getValue)
            .containsExactly(
                documentSent(
                    RESPONDENT_NOT_REPRESENTED.getParty(),
                    COVERSHEET_RESPONDENT_ENGLISH, ORDER_DOCUMENT_ORIGINAL, LETTER_2_ID, now()
                ),
                documentSent(
                    RESPONDENT_NOT_REPRESENTED.getParty(),
                    COVERSHEET_RESPONDENT, ORDER_DOCUMENT, LETTER_4_ID, now()
                )
            );
    }

    @Test
    void shouldNotifyRepresentativesServedDigitallyWhenOrderTranslated() throws NotificationClientException {
        final CaseData caseData = caseData();
        when(concurrencyHelper.startEvent(any(), any(String.class))).thenAnswer(i -> StartEventResponse.builder()
            .caseDetails(asCaseDetails(caseData))
            .eventId(i.getArgument(1))
            .token("token")
            .build());

        postSubmittedEvent(caseData);


        verify(notificationClient, timeout(ASYNC_METHOD_CALL_TIMEOUT)).sendEmail(
            ITEM_TRANSLATED_NOTIFICATION_TEMPLATE,
            REPRESENTATIVE_DIGITAL.getValue().getEmail(),
            NOTIFICATION_PARAMETERS,
            notificationReference(CASE_ID)
        );
    }

    @Test
    void shouldNotifyRepresentativesServedByEmailWhenOrderTranslated() throws NotificationClientException {
        final CaseData caseData = caseData();
        when(concurrencyHelper.startEvent(any(), any(String.class))).thenAnswer(i -> StartEventResponse.builder()
            .caseDetails(asCaseDetails(caseData))
            .eventId(i.getArgument(1))
            .token("token")
            .build());

        postSubmittedEvent(caseData);

        verify(notificationClient, timeout(ASYNC_METHOD_CALL_TIMEOUT)).sendEmail(
            ITEM_TRANSLATED_NOTIFICATION_TEMPLATE,
            REPRESENTATIVE_EMAIL.getValue().getEmail(),
            NOTIFICATION_PARAMETERS,
            notificationReference(CASE_ID)
        );
    }

    @Test
    void shouldFinaliseDocumentsSubmitted() {
        final CaseData caseData = caseData();

        when(concurrencyHelper.startEvent(any(), any(String.class))).thenAnswer(i -> StartEventResponse.builder()
            .caseDetails(asCaseDetails(caseData))
            .eventId(i.getArgument(1))
            .token("token")
            .build());

        postSubmittedEvent(caseData);

        verify(concurrencyHelper).submitEvent(any(), any(), caseDataDelta.capture());

        // should have translated document
        assertThat(caseDataDelta.getValue())
            .extracting("noticeOfProceedingsBundle")
            .isEqualTo(List.of(element(UUID_3, DocumentBundle.builder()
                .document(DocumentReference.builder()
                    .filename("noticeo_c6.pdf")
                    .build())
                .translatedDocument(DocumentReference.buildFromDocument(UPLOADED_TRANSFORMED_DOCUMENT))
                .translationRequirements(ENGLISH_TO_WELSH)
                .translationUploadDateTime(now())
                .build()
            ), element(UUID_4, DocumentBundle.builder()
                .document(DocumentReference.builder()
                    .filename("noticeo_c6a.pdf")
                    .build())
                .translationRequirements(ENGLISH_TO_WELSH)
                .build()
            )));

        // should have cleared temp fields
        assertThat(caseDataDelta.getValue()).extracting("uploadTranslationsOriginalDoc",
            "uploadTranslationsRelatedToDocument", "uploadTranslationsTranslatedDoc")
            .containsExactly(null, null, null);
    }

    private CaseData caseData() {
        return CASE_DATA_WITH_ALL_ORDERS.toBuilder()
            .id(CASE_ID)
            .court(court)
            .uploadTranslationsEventData(UploadTranslationsEventData.builder()
                .uploadTranslationsRelatedToDocument(RENDERED_DYNAMIC_LIST.toBuilder()
                    .value(dlElement(UUID_3, "Notice of proceedings (C6)"))
                    .build())
                .uploadTranslationsOriginalDoc(DocumentReference.builder()
                    .filename("noticeo_c6.pdf")
                    .build())
                .uploadTranslationsTranslatedDoc(TEST_DOCUMENT)
                .build())
            .children1(List.of(element(Child.builder()
                .party(ChildParty.builder()
                    .lastName("ChildLast")
                    .dateOfBirth(LocalDate.of(2012, 1, 2))
                    .build())
                .build())))
            .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .localAuthorities(wrapElementsWithUUIDs(LocalAuthority.builder()
                .id(LOCAL_AUTHORITY_1_CODE)
                .designated(YES.getValue())
                .email(LOCAL_AUTHORITY_1_INBOX)
                .build()))
            .orderCollection(List.of(element(UUID_1, GeneratedOrder.builder()
                .orderType("C32A_CARE_ORDER")
                .type(ORDER_TYPE)
                .translationUploadDateTime(now().plusSeconds(2))
                .translatedDocument(TRANSLATED_ORDER)
                .document(ORIGINAL_ORDER)
                .translationRequirements(ENGLISH_TO_WELSH)
                .build())))
            .respondents1(wrapElements(RESPONDENT_NOT_REPRESENTED, RESPONDENT_WITHOUT_ADDRESS, RESPONDENT_REPRESENTED))
            .representatives(List.of(REPRESENTATIVE_POST, REPRESENTATIVE_DIGITAL, REPRESENTATIVE_EMAIL))
            .build();
    }

    private static Map<String, Object> getExpectedParametersMap(String orderType) {
        return Map.of(
            "childLastName", "ChildLast",
            "docType", orderType,
            "callout", "^Jones, FMN1, hearing 3 Jan 2010",
            "courtName", "Family Court",
            "caseUrl", "http://fake-url/cases/case-details/1614860986487554"
        );
    }
}
