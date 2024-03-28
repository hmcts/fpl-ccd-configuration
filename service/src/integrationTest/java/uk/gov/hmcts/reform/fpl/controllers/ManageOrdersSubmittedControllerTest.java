package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.docmosis.DocmosisHelper;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.SentDocuments;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.EventService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.ccd.CCDConcurrencyHelper;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisCoverDocumentsService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocumentConversionService;
import uk.gov.hmcts.reform.fpl.service.email.EmailService;
import uk.gov.hmcts.reform.fpl.service.translation.TranslationRequestFormCreationService;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;
import uk.gov.service.notify.NotificationClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_ADMIN_EMAIL;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_CTSC_EMAIL;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_INBOX;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_AMENDED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.testingsupport.IntegrationTestConstants.COVERSHEET_PDF;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkUntil;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkUntilSecs;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElementsWithUUIDs;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParametersMap;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParametersMapForRepresentatives;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.documentSent;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.printRequest;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testAddress;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinaries;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;
import static uk.gov.hmcts.reform.fpl.utils.matchers.JsonMatcher.eqJson;

@WebMvcTest(ManageOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ManageOrdersSubmittedControllerTest extends AbstractCallbackTest {

    private static final Long CASE_ID = 1614860986487554L;
    private static final String NOTIFICATION_REFERENCE = "localhost/" + CASE_ID;
    private static final String FAMILY_MAN_CASE_NUMBER = "FMN1";
    private static final UUID LETTER_1_ID = randomUUID();
    private static final UUID LETTER_2_ID = randomUUID();
    private static final Document ORDER_DOCUMENT = testDocument();
    private static final Document COVERSHEET_REPRESENTATIVE = testDocument();
    private static final Document COVERSHEET_RESPONDENT = testDocument();
    private static final byte[] ORDER_BINARY = testDocumentBinaries();
    private static final byte[] COVERSHEET_REPRESENTATIVE_BINARY = testDocumentBinaries();
    private static final byte[] COVERSHEET_RESPONDENT_BINARY = testDocumentBinaries();
    private static final byte[] DOCUMENT_PDF_BINARIES = readBytes("documents/document1.pdf");
    private static final DocmosisDocument DOCMOSIS_PDF_DOCUMENT = testDocmosisDocument(DOCUMENT_PDF_BINARIES)
        .toBuilder().documentTitle("pdf.pdf").build();
    private static final DocumentReference ORDER = testDocumentReference();
    private static final String ORDER_TYPE = "Care order (C32A)";
    private static final Map<String, Object> NOTIFICATION_PARAMETERS = getExpectedParametersMap(ORDER_TYPE, true);
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
    private static final LanguageTranslationRequirement TRANSLATION_REQUIREMENTS =
        LanguageTranslationRequirement.ENGLISH_TO_WELSH;

    @Captor
    private ArgumentCaptor<Map<String, Object>> caseDataDelta;

    @Captor
    private ArgumentCaptor<LetterWithPdfsRequest> printRequest;

    @MockBean
    private SendLetterApi sendLetterApi;

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    TranslationRequestFormCreationService translationRequestFormCreationService;

    @MockBean
    DocmosisHelper docmosisHelper;

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

    @SpyBean
    private EventService eventPublisher;

    @MockBean
    private EmailService emailService;

    @MockBean
    private CafcassNotificationService cafcassNotificationService;

    @Captor
    private ArgumentCaptor<CafcassRequestEmailContentProvider> cafcassRequestEmailContentProviderArgumentCaptor;


    ManageOrdersSubmittedControllerTest() {
        super("manage-orders");
    }

    @BeforeEach
    void mocks() {
        givenFplService();
        when(documentDownloadService.downloadDocument(anyString()))
            .thenReturn(ORDER_BINARY);
        when(uploadDocumentService.uploadPDF(eq(ORDER_BINARY), any()))
            .thenReturn(ORDER_DOCUMENT);
        when(documentConversionService.convertToPdf(ORDER))
            .thenReturn(ORDER);
        when(documentConversionService.convertToPdfBytes(ORDER))
            .thenReturn(ORDER_BINARY);
        when(documentService.createCoverDocuments(any(), any(), eq(REPRESENTATIVE_POST.getValue()), any()))
            .thenReturn(DocmosisDocument.builder().bytes(COVERSHEET_REPRESENTATIVE_BINARY).build());
        when(uploadDocumentService.uploadPDF(COVERSHEET_REPRESENTATIVE_BINARY, COVERSHEET_PDF))
            .thenReturn(COVERSHEET_REPRESENTATIVE);

        when(documentService.createCoverDocuments(any(), any(), eq(RESPONDENT_NOT_REPRESENTED.getParty()),
            any()))
            .thenReturn(DocmosisDocument.builder().bytes(COVERSHEET_RESPONDENT_BINARY).build());
        when(uploadDocumentService.uploadPDF(COVERSHEET_RESPONDENT_BINARY, COVERSHEET_PDF))
            .thenReturn(COVERSHEET_RESPONDENT);


        when(sendLetterApi.sendLetter(any(), any(LetterWithPdfsRequest.class)))
            .thenReturn(new SendLetterResponse(LETTER_1_ID), new SendLetterResponse(LETTER_2_ID));
        when(translationRequestFormCreationService.buildTranslationRequestDocuments(any()))
            .thenReturn(DOCMOSIS_PDF_DOCUMENT);
        when(docmosisHelper.extractPdfContent(any())).thenReturn("Some content");
    }

    @Test
    void shouldSendOrdersByPostWhenOrderIssued() {
        CaseData caseData = caseData();

        when(concurrencyHelper.startEvent(any(), any(String.class))).thenAnswer(i -> StartEventResponse.builder()
            .caseDetails(asCaseDetails(caseData))
            .eventId(i.getArgument(1))
            .token("token")
            .build());

        postSubmittedEvent(caseData);

        verify(sendLetterApi, timeout(ASYNC_METHOD_CALL_TIMEOUT).times(2)).sendLetter(eq(SERVICE_AUTH_TOKEN),
            printRequest.capture());
        verify(concurrencyHelper, times(2)).submitEvent(any(), eq(CASE_ID), caseDataDelta.capture());

        LetterWithPdfsRequest expectedPrintRequest1 = printRequest(
            CASE_ID, ORDER, REPRESENTATIVE_POST.getValue(), COVERSHEET_REPRESENTATIVE_BINARY, ORDER_BINARY
        );

        LetterWithPdfsRequest expectedPrintRequest2 = printRequest(
            CASE_ID, ORDER, RESPONDENT_NOT_REPRESENTED.getParty(), COVERSHEET_RESPONDENT_BINARY, ORDER_BINARY
        );

        assertThat(printRequest.getAllValues()).usingRecursiveComparison()
            .isEqualTo(List.of(expectedPrintRequest1, expectedPrintRequest2));

        List<Element<SentDocuments>> documentsSent = mapper.convertValue(
            caseDataDelta.getValue().get("documentsSentToParties"), new TypeReference<>() {}
        );

        SentDocument expectedRepresentativeDocument = documentSent(
            REPRESENTATIVE_POST.getValue(), COVERSHEET_REPRESENTATIVE, ORDER_DOCUMENT, LETTER_1_ID, now()
        );

        SentDocument expectedRespondentDocument = documentSent(
            RESPONDENT_NOT_REPRESENTED.getParty(), COVERSHEET_RESPONDENT, ORDER_DOCUMENT, LETTER_2_ID, now()
        );

        assertThat(documentsSent.get(0).getValue().getDocumentsSentToParty())
            .extracting(Element::getValue)
            .containsExactly(expectedRepresentativeDocument);

        assertThat(documentsSent.get(1).getValue().getDocumentsSentToParty())
            .extracting(Element::getValue)
            .containsExactly(expectedRespondentDocument);
        verifyCafcassOrderNotification();
    }

    private void verifyCafcassOrderNotification() {
        checkUntil(() -> verify(cafcassNotificationService).sendEmail(
            isA(CaseData.class), any(), cafcassRequestEmailContentProviderArgumentCaptor.capture(), any()
        ));
        assertThat(cafcassRequestEmailContentProviderArgumentCaptor.getValue())
            .isEqualTo(CafcassRequestEmailContentProvider.ORDER);
    }

    @Test
    void shouldNotifyRepresentativesServedDigitallyWhenOrderIssued() {
        CaseData caseData = caseData();

        when(concurrencyHelper.startEvent(any(), any(String.class))).thenAnswer(i -> StartEventResponse.builder()
            .caseDetails(asCaseDetails(caseData))
            .eventId(i.getArgument(1))
            .token("token")
            .build());

        postSubmittedEvent(caseData);

        verifyCafcassOrderNotification();

        checkUntil(() -> verify(notificationClient, timeout(ASYNC_METHOD_CALL_TIMEOUT)).sendEmail(
            eq(ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES),
            eq(REPRESENTATIVE_DIGITAL.getValue().getEmail()), eqJson(NOTIFICATION_PARAMETERS),
            eq(NOTIFICATION_REFERENCE)
        ));
    }

    @Test
    void shouldNotifyRepresentativesServedByEmailWhenOrderIssued() {
        CaseData caseData = caseData();
        when(concurrencyHelper.startEvent(any(), any(String.class))).thenAnswer(i -> StartEventResponse.builder()
            .caseDetails(asCaseDetails(caseData))
            .eventId(i.getArgument(1))
            .token("token")
            .build());

        postSubmittedEvent(caseData);

        checkUntil(() -> verify(notificationClient, timeout(ASYNC_METHOD_CALL_TIMEOUT)).sendEmail(
            eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES), eq(REPRESENTATIVE_EMAIL.getValue().getEmail()),
            eqJson(getExpectedParametersMapForRepresentatives(ORDER_TYPE, true)), eq(NOTIFICATION_REFERENCE)
        ));
        verifyCafcassOrderNotification();
    }

    @Test
    void shouldNotifyLocalAuthorityWhenOrderIssued() {
        CaseData caseData = caseData();
        when(concurrencyHelper.startEvent(any(), any(String.class))).thenAnswer(i -> StartEventResponse.builder()
            .caseDetails(asCaseDetails(caseData))
            .eventId(i.getArgument(1))
            .token("token")
            .build());

        postSubmittedEvent(caseData);

        checkUntil(() -> verify(notificationClient, timeout(ASYNC_METHOD_CALL_TIMEOUT)).sendEmail(
            eq(ORDER_GENERATED_NOTIFICATION_TEMPLATE_FOR_LA_AND_DIGITAL_REPRESENTATIVES),
            eq(LOCAL_AUTHORITY_1_INBOX), eqJson(NOTIFICATION_PARAMETERS),
            eq(NOTIFICATION_REFERENCE)
        ));
        verifyCafcassOrderNotification();
    }

    @Test
    void shouldNotifyAdminWhenOrderIssued() {
        CaseData caseData = caseData();
        when(concurrencyHelper.startEvent(any(), any(String.class))).thenAnswer(i -> StartEventResponse.builder()
            .caseDetails(asCaseDetails(caseData))
            .eventId(i.getArgument(1))
            .token("token")
            .build());

        postSubmittedEvent(caseData);

        checkUntil(() -> verify(notificationClient, timeout(ASYNC_METHOD_CALL_TIMEOUT)).sendEmail(
            eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN), eq(DEFAULT_ADMIN_EMAIL),
            eqJson(NOTIFICATION_PARAMETERS), eq(NOTIFICATION_REFERENCE)
        ));
        verifyCafcassOrderNotification();
    }

    @Test
    void shouldNotifyCtscWhenEnabledWhenOrderIssued() {
        CaseData caseData = caseData().toBuilder().sendToCtsc("Yes").build();
        when(concurrencyHelper.startEvent(any(), any(String.class))).thenAnswer(i -> StartEventResponse.builder()
            .caseDetails(asCaseDetails(caseData))
            .eventId(i.getArgument(1))
            .token("token")
            .build());

        postSubmittedEvent(caseData);

        checkUntil(() -> {
            verify(notificationClient, timeout(ASYNC_METHOD_CALL_TIMEOUT)).sendEmail(
                eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN), eq(DEFAULT_CTSC_EMAIL),
                eqJson(NOTIFICATION_PARAMETERS), eq(NOTIFICATION_REFERENCE)
            );

            verify(notificationClient, never()).sendEmail(
                eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN), eq(DEFAULT_ADMIN_EMAIL), any(), any()
            );
        });
        verifyCafcassOrderNotification();
    }

    @Test
    void shouldNotifyTranslationTeamIfTranslationIsRequired() {
        CaseData caseData = caseData().toBuilder().orderCollection(
            wrapElements(GeneratedOrder.builder()
                .orderType("C32A_CARE_ORDER")
                .type(ORDER_TYPE)
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeTitle(HIS_HONOUR_JUDGE)
                    .judgeLastName("Dredd")
                    .build())
                .dateTimeIssued(now().plusSeconds(2))
                .approvalDate(dateNow())
                .document(ORDER)
                .translationRequirements(TRANSLATION_REQUIREMENTS)
                .build())
        ).build();
        when(concurrencyHelper.startEvent(any(), any(String.class))).thenAnswer(i -> StartEventResponse.builder()
            .caseDetails(asCaseDetails(caseData))
            .eventId(i.getArgument(1))
            .token("token")
            .build());

        postSubmittedEvent(caseData);

        checkUntil(() -> checkUntil(() -> verify(emailService).sendEmail(eq("sender@example.com"), any())));
        verifyCafcassOrderNotification();
    }

    @Test
    void shouldNotifyTranslationTeamIfTranslationIsNotRequired() {
        CaseData caseData = caseData();
        when(concurrencyHelper.startEvent(any(), any(String.class))).thenAnswer(i -> StartEventResponse.builder()
            .caseDetails(asCaseDetails(caseData))
            .eventId(i.getArgument(1))
            .token("token")
            .build());

        postSubmittedEvent(caseData);

        checkUntil(() -> verify(emailService, never()).sendEmail(eq("sender@example.com"), any()));
        verifyCafcassOrderNotification();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSendAmendedNotificationToLocalAuthorityWhenAmendedOrder() {
        CaseData caseData = caseData();
        when(concurrencyHelper.startEvent(any(), any(String.class))).thenAnswer(i -> StartEventResponse.builder()
            .caseDetails(asCaseDetails(caseData))
            .eventId(i.getArgument(1))
            .token("token")
            .build());

        CaseData caseDataBefore = caseDataBefore();
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(asCaseDetails(caseData))
            .caseDetailsBefore(asCaseDetails(caseDataBefore))
            .build();

        postSubmittedEvent(request);

        checkUntil(() -> verify(notificationClient, timeout(ASYNC_METHOD_CALL_TIMEOUT)).sendEmail(
            eq(ORDER_AMENDED_NOTIFICATION_TEMPLATE),
            eq(LOCAL_AUTHORITY_1_INBOX), eqJson(NOTIFICATION_PARAMETERS),
            eq(NOTIFICATION_REFERENCE)
        ));
    }

    @Test
    void shouldSendOrdersByPostWhenOrderAmended() {
        CaseData caseData = caseData();
        when(concurrencyHelper.startEvent(any(), any(String.class))).thenAnswer(i -> StartEventResponse.builder()
            .caseDetails(asCaseDetails(caseData))
            .eventId(i.getArgument(1))
            .token("token")
            .build());

        CaseData caseDataBefore = caseDataBefore();

        when(concurrencyHelper.startEvent(any(), any(String.class))).thenAnswer(i -> StartEventResponse.builder()
            .caseDetails(asCaseDetails(caseData))
            .eventId(i.getArgument(1))
            .token("token")
            .build());

        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(asCaseDetails(caseData))
            .caseDetailsBefore(asCaseDetails(caseDataBefore))
            .build();

        postSubmittedEvent(request);

        checkUntilSecs(() -> {
            verify(sendLetterApi, timeout(ASYNC_METHOD_CALL_TIMEOUT).times(2)).sendLetter(eq(SERVICE_AUTH_TOKEN),
                printRequest.capture());
            verify(concurrencyHelper, times(2)).submitEvent(any(), eq(CASE_ID), caseDataDelta.capture());
        }, 2);

        LetterWithPdfsRequest expectedPrintRequest1 = printRequest(
            CASE_ID, ORDER, REPRESENTATIVE_POST.getValue(), COVERSHEET_REPRESENTATIVE_BINARY, ORDER_BINARY
        );

        LetterWithPdfsRequest expectedPrintRequest2 = printRequest(
            CASE_ID, ORDER, RESPONDENT_NOT_REPRESENTED.getParty(), COVERSHEET_RESPONDENT_BINARY, ORDER_BINARY
        );

        assertThat(printRequest.getAllValues()).usingRecursiveComparison()
            .isEqualTo(List.of(expectedPrintRequest1, expectedPrintRequest2));

        List<Element<SentDocuments>> documentsSent = mapper.convertValue(
            caseDataDelta.getValue().get("documentsSentToParties"), new TypeReference<>() {}
        );

        SentDocument expectedRepresentativeDocument = documentSent(
            REPRESENTATIVE_POST.getValue(), COVERSHEET_REPRESENTATIVE, ORDER_DOCUMENT, LETTER_1_ID, now()
        );

        SentDocument expectedRespondentDocument = documentSent(
            RESPONDENT_NOT_REPRESENTED.getParty(), COVERSHEET_RESPONDENT, ORDER_DOCUMENT, LETTER_2_ID, now()
        );

        assertThat(documentsSent.get(0).getValue().getDocumentsSentToParty())
            .extracting(Element::getValue)
            .containsExactly(expectedRepresentativeDocument);

        assertThat(documentsSent.get(1).getValue().getDocumentsSentToParty())
            .extracting(Element::getValue)
            .containsExactly(expectedRespondentDocument);
    }

    @Test
    void shouldNotifyRepresentativesServedDigitallyWhenOrderAmended() {
        CaseData caseData = caseData();
        when(concurrencyHelper.startEvent(any(), any(String.class))).thenAnswer(i -> StartEventResponse.builder()
            .caseDetails(asCaseDetails(caseData))
            .eventId(i.getArgument(1))
            .token("token")
            .build());

        CaseData caseDataBefore = caseDataBefore();
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(asCaseDetails(caseData))
            .caseDetailsBefore(asCaseDetails(caseDataBefore))
            .build();

        postSubmittedEvent(request);

        checkUntil(() -> verify(notificationClient, timeout(ASYNC_METHOD_CALL_TIMEOUT)).sendEmail(
            eq(ORDER_AMENDED_NOTIFICATION_TEMPLATE),
            eq(REPRESENTATIVE_DIGITAL.getValue().getEmail()), eqJson(NOTIFICATION_PARAMETERS),
            eq(NOTIFICATION_REFERENCE)
        ));
    }

    @Test
    void shouldNotifyRepresentativesServedByEmailWhenOrderAmended() {
        CaseData caseData = caseData();
        when(concurrencyHelper.startEvent(any(), any(String.class))).thenAnswer(i -> StartEventResponse.builder()
            .caseDetails(asCaseDetails(caseData))
            .eventId(i.getArgument(1))
            .token("token")
            .build());

        CaseData caseDataBefore = caseDataBefore();
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(asCaseDetails(caseData))
            .caseDetailsBefore(asCaseDetails(caseDataBefore))
            .build();

        postSubmittedEvent(request);

        checkUntil(() -> verify(notificationClient, timeout(ASYNC_METHOD_CALL_TIMEOUT)).sendEmail(
            eq(ORDER_AMENDED_NOTIFICATION_TEMPLATE), eq(REPRESENTATIVE_EMAIL.getValue().getEmail()),
            eqJson(getExpectedParametersMapForRepresentatives(ORDER_TYPE, true)), eq(NOTIFICATION_REFERENCE)
        ));
    }

    private CaseData caseData() {
        return CaseData.builder()
            .id(CASE_ID)
            .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .localAuthorities(wrapElementsWithUUIDs(LocalAuthority.builder()
                .id(LOCAL_AUTHORITY_1_CODE)
                .designated(YES.getValue())
                .email(LOCAL_AUTHORITY_1_INBOX)
                .build()))
            .orderCollection(wrapElements(GeneratedOrder.builder()
                .orderType("C32A_CARE_ORDER")
                .type(ORDER_TYPE)
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeTitle(HIS_HONOUR_JUDGE)
                    .judgeLastName("Dredd")
                    .build())
                .dateTimeIssued(now().plusSeconds(2))
                .approvalDate(dateNow())
                .document(ORDER)
                .build()))
            .respondents1(wrapElements(RESPONDENT_NOT_REPRESENTED, RESPONDENT_WITHOUT_ADDRESS, RESPONDENT_REPRESENTED))
            .representatives(List.of(REPRESENTATIVE_POST, REPRESENTATIVE_DIGITAL, REPRESENTATIVE_EMAIL))
            .build();
    }

    private CaseData caseDataBefore() {
        return CaseData.builder()
            .id(CASE_ID)
            .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .orderCollection(wrapElements(GeneratedOrder.builder()
                .orderType("C32A_CARE_ORDER")
                .type(ORDER_TYPE)
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeTitle(HIS_HONOUR_JUDGE)
                    .judgeLastName("Dredd")
                    .build())
                .dateTimeIssued(now())
                .approvalDate(dateNow())
                .document(ORDER)
                .build()))
            .respondents1(wrapElements(RESPONDENT_NOT_REPRESENTED, RESPONDENT_WITHOUT_ADDRESS, RESPONDENT_REPRESENTED))
            .representatives(List.of(REPRESENTATIVE_POST, REPRESENTATIVE_DIGITAL, REPRESENTATIVE_EMAIL))
            .build();
    }
}
