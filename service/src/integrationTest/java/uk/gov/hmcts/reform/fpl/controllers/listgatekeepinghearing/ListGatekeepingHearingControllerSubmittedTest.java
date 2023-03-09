package uk.gov.hmcts.reform.fpl.controllers.listgatekeepinghearing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.controllers.ListGatekeepingHearingController;
import uk.gov.hmcts.reform.fpl.docmosis.DocmosisHelper;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.GatekeepingOrderSealDecision;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;
import uk.gov.hmcts.reform.fpl.model.document.SealType;
import uk.gov.hmcts.reform.fpl.model.event.GatekeepingOrderEventData;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.EventService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisCoverDocumentsService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocumentConversionService;
import uk.gov.hmcts.reform.fpl.service.email.EmailService;
import uk.gov.hmcts.reform.fpl.service.others.OtherRecipientsInbox;
import uk.gov.hmcts.reform.fpl.service.translation.TranslationRequestFormCreationService;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_3_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_3_INBOX;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_NEW_HEARING;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_AND_NOP_ISSUED_CAFCASS;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_AND_NOP_ISSUED_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_AND_NOP_ISSUED_LA;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.ISSUE_RESOLUTION;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.WELSH_TO_ENGLISH;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING_LISTING;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_NAME;
import static uk.gov.hmcts.reform.fpl.testingsupport.IntegrationTestConstants.CAFCASS_EMAIL;
import static uk.gov.hmcts.reform.fpl.testingsupport.IntegrationTestConstants.COVERSHEET_PDF;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkThat;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkUntil;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRepresentatives;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.DOCUMENT_CONTENT;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.documentSent;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.printRequest;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testAddress;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinary;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(ListGatekeepingHearingController.class)
@OverrideAutoConfiguration(enabled = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ListGatekeepingHearingControllerSubmittedTest extends ListGatekeepingHearingControllerTest {
    private static final Long CASE_ID = 1L;
    private static final String FAMILY_MAN_NUMBER = "FMN1";
    private static final long ASYNC_METHOD_CALL_TIMEOUT = 10000;
    private static final String SEND_DOCUMENT_EVENT = "internal-change-SEND_DOCUMENT";
    private static final DocumentReference SDO_DOCUMENT = testDocumentReference();
    private static final CaseData GATEKEEPING_LISTING_CASE_DATA = CaseData.builder().state(GATEKEEPING_LISTING).build();
    private static final String NOTIFICATION_REFERENCE = "localhost/" + CASE_ID;
    private static final byte[] DOCUMENT_PDF_BINARIES = readBytes("documents/document1.pdf");
    private static final DocmosisDocument DOCMOSIS_PDF_DOCUMENT = testDocmosisDocument(DOCUMENT_PDF_BINARIES)
        .toBuilder().documentTitle("pdf.pdf").build();
    private static final byte[] APPLICATION_BINARY = DOCUMENT_CONTENT;
    private static final DocumentReference SEALED_DOCUMENT = testDocumentReference();
    private static final DocumentReference URGENT_HEARING_ORDER_DOCUMENT = testDocumentReference();
    private static final byte[] NOTICE_OF_HEARING_BINARY = testDocumentBinary();
    private static final String DOC_UPLOADER_NAME = "MOCK";
    private static final LocalDate DATE_ADDED = LocalDate.of(2023, 2, 4);
    private static final DocumentReference C6_DOCUMENT = testDocumentReference("notice_of_proceedings_c6.pdf");
    private static final DocumentReference C6A_DOCUMENT = testDocumentReference("notice_of_proceedings_c6a.pdf");
    private static final UUID LETTER_1_ID = randomUUID();
    private static final UUID LETTER_2_ID = randomUUID();
    private static final Document NOTICE_OF_HEARING_DOCUMENT = testDocument();
    private static final Document COVERSHEET_REPRESENTATIVE = testDocument();
    private static final Document COVERSHEET_RESPONDENT = testDocument();
    private static final byte[] COVERSHEET_REPRESENTATIVE_BINARY = testDocumentBinary();
    private static final byte[] COVERSHEET_RESPONDENT_BINARY = testDocumentBinary();

    private final Element<HearingBooking> hearingWithoutNotice = element(HearingBooking.builder()
        .type(CASE_MANAGEMENT)
        .startDate(LocalDateTime.of(2050, 5, 20, 13, 0))
        .endDate(LocalDateTime.of(2050, 5, 20, 14, 0))
        .noticeOfHearing(null)
        .build());

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

    private static final Respondent RESPONDENT_REPRESENTED = Respondent.builder()
        .party(RespondentParty.builder()
            .firstName("George")
            .lastName("Jones")
            .address(testAddress())
            .build())
        .representedBy(wrapElements(REPRESENTATIVE_POST.getId(), REPRESENTATIVE_DIGITAL.getId()))
        .build();

    private static final Child CHILDREN = Child.builder()
        .party(ChildParty.builder()
            .firstName("Jade")
            .lastName("Connor")
            .dateOfBirth(LocalDate.now())
            .address(testAddress())
            .build())
        .build();

    @Captor
    private ArgumentCaptor<Map<String, Object>> caseCaptor;
    @MockBean
    private NotificationClient notificationClient;
    @MockBean
    private CafcassNotificationService cafcassNotificationService;
    @MockBean
    private EmailService emailService;
    @MockBean
    private DocumentSealingService sealingService;
    @MockBean
    TranslationRequestFormCreationService translationRequestFormCreationService;
    @MockBean
    private DocumentDownloadService documentDownloadService;
    @MockBean
    private UploadDocumentService uploadDocumentService;
    @MockBean
    private DocumentConversionService documentConversionService;
    @MockBean
    DocmosisHelper docmosisHelper;
    @MockBean
    private DocmosisCoverDocumentsService documentService;
    @Captor
    private ArgumentCaptor<LetterWithPdfsRequest> printRequestCaptor;
    @SpyBean
    private EventService eventService;
    @MockBean
    private CoreCaseDataService coreCaseDataService;
    @MockBean
    private OtherRecipientsInbox otherRecipientsInbox;
    @MockBean
    private SendLetterApi sendLetterApi;

    ListGatekeepingHearingControllerSubmittedTest() {
        super("list-gatekeeping-hearing");
    }

    @BeforeEach
    void init() {
        when(translationRequestFormCreationService.buildTranslationRequestDocuments(any()))
            .thenReturn(DOCMOSIS_PDF_DOCUMENT);
        when(documentDownloadService.downloadDocument(any())).thenReturn(APPLICATION_BINARY);
        when(docmosisHelper.extractPdfContent(APPLICATION_BINARY)).thenReturn("Some content");
        when(sealingService.sealDocument(eq(SDO_DOCUMENT), any(), any())).thenReturn(SEALED_DOCUMENT);
        when(sealingService.sealDocument(eq(URGENT_HEARING_ORDER_DOCUMENT), any(), any())).thenReturn(SEALED_DOCUMENT);
    }

    @Test
    void shouldNotTriggerEventsWhenDraft() {
        postSubmittedEvent(toCallBackRequest(buildCaseDataWithSDO(DRAFT), GATEKEEPING_LISTING_CASE_DATA));

        verifyNoInteractions(sealingService);
        verify(eventService, never()).publishEvent(any());
        verify(coreCaseDataService, never()).triggerEvent(any(), any(), any(), any(), any());
    }

    @Test
    void shouldDoNothingWhenNoHearingAddedOrUpdated() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(CASE_ID)
            .data(buildData(List.of(hearingWithoutNotice), hearingWithoutNotice.getId()))
            .build();

        postSubmittedEvent(caseDetails);

        verifyNoInteractions(notificationClient);
        verify(coreCaseDataService).triggerEvent(JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            "internal-update-case-summary",
            caseSummary("Yes", "Case management", LocalDate.of(2050, 5, 20)));
        verifyNoMoreInteractions(coreCaseDataService);
    }

    @Test
    void shouldTriggerEventWhenSDOSubmitted() {
        //CaseData.builder().state(GATEKEEPING_LISTING).build()
        postSubmittedEvent(toCallBackRequest(buildCaseDataWithSDO(SEALED), GATEKEEPING_LISTING_CASE_DATA));

        verifyNoInteractions(sealingService);
        verifyEmails(SDO_AND_NOP_ISSUED_CAFCASS, SDO_AND_NOP_ISSUED_CTSC, SDO_AND_NOP_ISSUED_LA);
        verifyNoMoreNotificationsSent();
    }

    @Test
    void shouldTriggerSendNoticeOfHearingEventForNewHearingWhenNoticeOfHearingPresent()
        throws NotificationClientException {
        final DocumentReference noticeOfHearing = testDocumentReference();

        final Element<HearingBooking> hearingWithNotice = element(HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(2050, 5, 20, 13, 0))
            .endDate(LocalDateTime.of(2050, 5, 20, 14, 0))
            .noticeOfHearing(noticeOfHearing)
            .venue("96")
            .build());

        final Element<HearingBooking> existingHearing = element(HearingBooking.builder()
            .type(ISSUE_RESOLUTION)
            .startDate(LocalDateTime.of(2020, 5, 20, 13, 0))
            .endDate(LocalDateTime.of(2020, 5, 20, 14, 0))
            .noticeOfHearing(testDocumentReference())
            .venue("162")
            .others(emptyList())
            .build());

        final CaseData cdb = CaseData.builder()
            .id(CASE_ID)
            .familyManCaseNumber(FAMILY_MAN_NUMBER)
            .caseLocalAuthority(LOCAL_AUTHORITY_3_CODE)
            .hearingDetails(List.of(existingHearing))
            .representatives(List.of(REPRESENTATIVE_DIGITAL, REPRESENTATIVE_EMAIL, REPRESENTATIVE_POST))
            .respondents1(wrapElements(RESPONDENT_REPRESENTED, RESPONDENT_NOT_REPRESENTED))
            .gatekeepingOrderRouter(GatekeepingOrderRoute.SERVICE)
            .standardDirectionOrder(StandardDirectionOrder.builder()
                .orderStatus(SEALED)
                .orderDoc(SDO_DOCUMENT)
                .build())
            .build();

        final CaseData cd = cdb.toBuilder()
            .hearingDetails(List.of(hearingWithNotice, existingHearing))
            .selectedHearingId(hearingWithNotice.getId())
//            .state(GATEKEEPING_LISTING)
            .build();

        givenFplService();

        given(documentDownloadService.downloadDocument(noticeOfHearing.getBinaryUrl()))
            .willReturn(NOTICE_OF_HEARING_BINARY);

        given(documentConversionService.convertToPdf(noticeOfHearing))
            .willReturn(noticeOfHearing);

        given(otherRecipientsInbox.getNonSelectedRecipients(
            EMAIL,
            cdb,
            emptyList(),
            element -> element.getValue().getEmail())
        ).willReturn(emptySet());

        given(sendLetterApi.sendLetter(eq(SERVICE_AUTH_TOKEN), any(LetterWithPdfsRequest.class)))
            .willReturn(new SendLetterResponse(LETTER_1_ID))
            .willReturn(new SendLetterResponse(LETTER_2_ID));

        given(uploadDocumentService.uploadPDF(NOTICE_OF_HEARING_BINARY, noticeOfHearing.getFilename()))
            .willReturn(NOTICE_OF_HEARING_DOCUMENT);
        given(uploadDocumentService.uploadPDF(COVERSHEET_REPRESENTATIVE_BINARY, COVERSHEET_PDF))
            .willReturn(COVERSHEET_REPRESENTATIVE);
        given(uploadDocumentService.uploadPDF(COVERSHEET_RESPONDENT_BINARY, COVERSHEET_PDF))
            .willReturn(COVERSHEET_RESPONDENT);

        given(documentService.createCoverDocuments(FAMILY_MAN_NUMBER, CASE_ID, REPRESENTATIVE_POST.getValue(),
            Language.ENGLISH))
            .willReturn(testDocmosisDocument(COVERSHEET_REPRESENTATIVE_BINARY));
        given(documentService.createCoverDocuments(FAMILY_MAN_NUMBER, CASE_ID, RESPONDENT_NOT_REPRESENTED.getParty(),
            Language.ENGLISH))
            .willReturn(testDocmosisDocument(COVERSHEET_RESPONDENT_BINARY));

        postSubmittedEvent(toCallBackRequest(cd, cdb));

        verify(notificationClient, timeout(ASYNC_METHOD_CALL_TIMEOUT)).sendEmail(
            eq(NOTICE_OF_NEW_HEARING),
            eq(LOCAL_AUTHORITY_3_INBOX),
            anyMap(),
            eq(NOTIFICATION_REFERENCE));

        verify(notificationClient, timeout(ASYNC_METHOD_CALL_TIMEOUT)).sendEmail(
            eq(NOTICE_OF_NEW_HEARING),
            eq(CAFCASS_EMAIL),
            anyMap(),
            eq(NOTIFICATION_REFERENCE));

        verify(notificationClient, timeout(ASYNC_METHOD_CALL_TIMEOUT)).sendEmail(
            eq(NOTICE_OF_NEW_HEARING),
            eq(REPRESENTATIVE_EMAIL.getValue().getEmail()),
            anyMap(),
            eq(NOTIFICATION_REFERENCE));

        verify(sendLetterApi, timeout(ASYNC_METHOD_CALL_TIMEOUT).times(2)).sendLetter(
            eq(SERVICE_AUTH_TOKEN),
            printRequestCaptor.capture());

        verify(coreCaseDataService, timeout(ASYNC_METHOD_CALL_TIMEOUT)).updateCase(
            eq(CASE_ID), caseCaptor.capture());

        LetterWithPdfsRequest expectedPrintRequest1 = printRequest(CASE_ID, noticeOfHearing,
            COVERSHEET_REPRESENTATIVE_BINARY, NOTICE_OF_HEARING_BINARY);

        LetterWithPdfsRequest expectedPrintRequest2 = printRequest(CASE_ID, noticeOfHearing,
            COVERSHEET_RESPONDENT_BINARY, NOTICE_OF_HEARING_BINARY);

        SentDocument expectedDocumentSentToRepresentative = documentSent(REPRESENTATIVE_POST.getValue(),
            COVERSHEET_REPRESENTATIVE, NOTICE_OF_HEARING_DOCUMENT, LETTER_1_ID, now());

        SentDocument expectedDocumentSentToRespondent = documentSent(RESPONDENT_NOT_REPRESENTED.getParty(),
            COVERSHEET_RESPONDENT, NOTICE_OF_HEARING_DOCUMENT, LETTER_2_ID, now());

        assertThat(printRequestCaptor.getAllValues()).usingRecursiveComparison()
            .isEqualTo(List.of(expectedPrintRequest1, expectedPrintRequest2));

        final CaseData caseUpdate = getCase(caseCaptor);

        assertThat(caseUpdate.getDocumentsSentToParties()).hasSize(2);

        assertThat(caseUpdate.getDocumentsSentToParties().get(0).getValue().getDocumentsSentToParty())
            .extracting(Element::getValue)
            .containsExactly(expectedDocumentSentToRepresentative);

        assertThat(caseUpdate.getDocumentsSentToParties().get(1).getValue().getDocumentsSentToParty())
            .extracting(Element::getValue)
            .containsExactly(expectedDocumentSentToRespondent);

        verify(coreCaseDataService).triggerEvent(eq(JURISDICTION), eq(CASE_TYPE), eq(CASE_ID),
            eq("internal-update-case-summary"), anyMap());

        verify(cafcassNotificationService,never()).sendEmail(any(), any(), any(), any());

//        verifyNoMoreInteractions(coreCaseDataService);
//        verifyNoInteractions(sealingService);
//        verifyEmails(SDO_AND_NOP_ISSUED_CAFCASS, SDO_AND_NOP_ISSUED_CTSC, SDO_AND_NOP_ISSUED_LA);
    }

    @Test
    void shouldTriggerEventWhenSDOSubmittedWithTranslation() {
        postSubmittedEvent(toCallBackRequest(buildCaseDataWithSDOToTranslate(SEALED), GATEKEEPING_LISTING_CASE_DATA));

        verifyNoInteractions(sealingService);
        verifyEmails(SDO_AND_NOP_ISSUED_CAFCASS, SDO_AND_NOP_ISSUED_CTSC, SDO_AND_NOP_ISSUED_LA);
        verifyEmailSentToTranslation();
        verifyNoMoreNotificationsSent();
    }

    @Test
    void shouldTriggerEventWhenSDOAndNoticeOfProceedingsSubmittedWithTranslation() {
        postSubmittedEvent(toCallBackRequest(buildCaseDataWithSDOAndNopToTranslate(SEALED),
            GATEKEEPING_LISTING_CASE_DATA));

        verifyNoInteractions(sealingService);
        verifyEmails(SDO_AND_NOP_ISSUED_CAFCASS, SDO_AND_NOP_ISSUED_CTSC, SDO_AND_NOP_ISSUED_LA);
        verifyEmailSentToTranslation(3);
        verifyNoMoreNotificationsSent();
    }

    @Test
    void shouldTriggerSendDocumentEventWhenSubmitted() {
        postSubmittedEvent(toCallBackRequest(buildCaseDataWithSDO(SEALED), GATEKEEPING_LISTING_CASE_DATA));

        verifyNoInteractions(sealingService);
        verify(coreCaseDataService).triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            SEND_DOCUMENT_EVENT,
            Map.of("documentToBeSent", SDO_DOCUMENT)
        );
    }

    @Test
    void shouldSealUploadedDocumentIfOrderStatusIsSealed() {
        CaseData caseData = buildCaseDataWithUploadedSDO(SEALED);
        postSubmittedEvent(toCallBackRequest(
            caseData, GATEKEEPING_LISTING_CASE_DATA
        ));

        verify(sealingService).sealDocument(eq(SDO_DOCUMENT), any(), eq(SealType.ENGLISH));
        verify(coreCaseDataService).triggerEvent(eq(caseData.getId()),eq("internal-change-add-gatekeeping"), any());
    }

    private void verifyEmails(String cafcassTemplate, String ctcsTemplate, String laTemplate) {
        checkUntil(() -> verify(notificationClient).sendEmail(
            eq(cafcassTemplate),
            eq(CAFCASS_EMAIL),
            anyMap(),
            eq(NOTIFICATION_REFERENCE)
        ));

        checkUntil(() -> verify(notificationClient).sendEmail(
            eq(laTemplate),
            eq("shared@test1.org.uk"),
            anyMap(),
            eq(NOTIFICATION_REFERENCE)
        ));

        checkUntil(() -> verify(notificationClient).sendEmail(
            eq(ctcsTemplate),
            eq("FamilyPublicLaw+ctsc@gmail.com"),
            anyMap(),
            eq(NOTIFICATION_REFERENCE)
        ));
    }

    private void verifyEmailSentToTranslation() {
        checkUntil(() -> verify(emailService).sendEmail(eq("sender@example.com"), any()));
    }

    private void verifyEmailSentToTranslation(int timesCalled) {
        checkUntil(() -> verify(emailService, times(timesCalled)).sendEmail(eq("sender@example.com"), any()));
    }

    private void verifyNoMoreNotificationsSent() {
        checkThat(() -> verifyNoMoreInteractions(notificationClient, emailService), Duration.ofSeconds(2));
    }

    private CaseData buildCaseDataWithUploadedSDO(OrderStatus status) {
        return baseCaseData()
            .gatekeepingOrderRouter(GatekeepingOrderRoute.UPLOAD)
            .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                .gatekeepingOrderSealDecision(GatekeepingOrderSealDecision.builder()
                    .orderStatus(status).build())
                .build())
            .standardDirectionOrder(StandardDirectionOrder.builder()
                .orderStatus(status)
                .orderDoc(SDO_DOCUMENT)
                .unsealedDocumentCopy(SDO_DOCUMENT)
                .uploader(DOC_UPLOADER_NAME)
                .dateOfUpload(DATE_ADDED)
                .translationRequirements(LanguageTranslationRequirement.NO)
                .build())
            .build();
    }

    private CaseData buildCaseDataWithSDO(OrderStatus status) {
        return baseCaseData()
            .gatekeepingOrderRouter(GatekeepingOrderRoute.SERVICE)
            .standardDirectionOrder(StandardDirectionOrder.builder()
                .orderStatus(status)
                .orderDoc(SDO_DOCUMENT)
                .build())
            .build();
    }

    private CaseData buildCaseDataWithSDOToTranslate(OrderStatus status) {
        return baseCaseData()
            .gatekeepingOrderRouter(GatekeepingOrderRoute.SERVICE)
            .standardDirectionOrder(StandardDirectionOrder.builder()
                .orderStatus(status)
                .orderDoc(SDO_DOCUMENT)
                .translationRequirements(WELSH_TO_ENGLISH)
                .build())
            .build();
    }

    private CaseData buildCaseDataWithSDOAndNopToTranslate(OrderStatus status) {
        return baseCaseData()
            .gatekeepingOrderRouter(GatekeepingOrderRoute.SERVICE)
            .standardDirectionOrder(StandardDirectionOrder.builder()
                .orderStatus(status)
                .orderDoc(SDO_DOCUMENT)
                .translationRequirements(WELSH_TO_ENGLISH)
                .build())
            .noticeOfProceedingsBundle(List.of(
                element(DocumentBundle.builder()
                    .document(C6_DOCUMENT)
                    .translationRequirements(WELSH_TO_ENGLISH)
                    .build()),
                element(DocumentBundle.builder()
                    .document(C6A_DOCUMENT)
                    .translationRequirements(WELSH_TO_ENGLISH)
                    .build())
            )).build();
    }

    private CaseData.CaseDataBuilder<?,?> baseCaseData() {
        return CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .hearingDetails(wrapElements(HearingBooking.builder()
                .startDate(LocalDateTime.of(2020, 10, 20, 11, 11, 11))
                .endDate(LocalDateTime.of(2020, 11, 20, 11, 11, 11))
                .build()))
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder()
                    .dateOfBirth(dateNow().plusDays(1))
                    .lastName("Moley")
                    .relationshipToChild("Uncle")
                    .build())
                .build()));
    }

    private Map<String, Object> buildData(List<Element<HearingBooking>> hearings, UUID selectedHearing) {
        return Map.of("hearingDetails", hearings,
            "selectedHearingId", selectedHearing,
            "caseLocalAuthority", LOCAL_AUTHORITY_1_CODE,
            "representatives", createRepresentatives(RepresentativeServingPreferences.EMAIL),
            ALL_PARTIES.getValue(),
            wrapElements(
                buildDirection("allParties1"),
                buildDirection("allParties2", LocalDateTime.of(2060, 1, 1, 13, 0, 0)),
                buildDirection("allParties3"),
                buildDirection("allParties4"),
                buildDirection("allParties5", LocalDateTime.of(2060, 2, 2, 14, 0, 0))),
            LOCAL_AUTHORITY.getValue(),
            wrapElements(
                buildDirection("la1", LocalDateTime.of(2060, 3, 3, 13, 0, 0)),
                buildDirection("la2", LocalDateTime.of(2060, 4, 4, 14, 0, 0)),
                buildDirection("la3"),
                buildDirection("la4"),
                buildDirection("la5", LocalDateTime.of(2060, 5, 5, 15, 0, 0)),
                buildDirection("la6"),
                buildDirection("la7", LocalDateTime.of(2060, 6, 6, 16, 0, 0))),
            PARENTS_AND_RESPONDENTS.getValue(), wrapElements(
                buildDirection("p&r1")),
            CAFCASS.getValue(), wrapElements(
                buildDirection("cafcass1"),
                buildDirection("cafcass2", LocalDateTime.of(2060, 7, 7, 17, 0, 0)),
                buildDirection("cafcass3")),
            OTHERS.getValue(), wrapElements(
                buildDirection("others1")),
            COURT.getValue(), wrapElements(
                buildDirection("court1", LocalDateTime.of(2060, 8, 8, 18, 0, 0))));
    }

    private Direction buildDirection(String text) {
        return Direction.builder().directionText(text).build();
    }

    private Direction buildDirection(String text, LocalDateTime dateTime) {
        return Direction.builder().directionText(text).dateToBeCompletedBy(dateTime).build();
    }

    private Map<String, Object> caseSummary(String hasNextHearing, String hearingType, LocalDate hearingDate) {
        return caseConverter.toMap(SyntheticCaseSummary.builder()
            .caseSummaryHasNextHearing(hasNextHearing)
            .caseSummaryNextHearingType(hearingType)
            .caseSummaryNextHearingDate(hearingDate)
            .caseSummaryCourtName(COURT_NAME)
            .caseSummaryLanguageRequirement("No")
            .caseSummaryLALanguageRequirement("No")
            .caseSummaryHighCourtCase("No")
            .caseSummaryLAHighCourtCase("No")
            .caseSummaryLATabHidden("Yes")
            .build());
    }
}
