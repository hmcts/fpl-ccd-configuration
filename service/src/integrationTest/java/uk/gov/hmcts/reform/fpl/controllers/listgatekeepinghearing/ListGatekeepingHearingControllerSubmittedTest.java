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
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.ListGatekeepingHearingController;
import uk.gov.hmcts.reform.fpl.docmosis.DocmosisHelper;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute;
import uk.gov.hmcts.reform.fpl.model.CaseData;
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
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_CTSC_EMAIL;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_3_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_3_INBOX;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_NEW_HEARING;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_AND_NOP_ISSUED_CAFCASS;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_AND_NOP_ISSUED_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_AND_NOP_ISSUED_LA;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.WELSH_TO_ENGLISH;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING;
import static uk.gov.hmcts.reform.fpl.testingsupport.IntegrationTestConstants.CAFCASS_EMAIL;
import static uk.gov.hmcts.reform.fpl.testingsupport.IntegrationTestConstants.COVERSHEET_PDF;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkThat;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkUntil;
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
class ListGatekeepingHearingControllerSubmittedTest extends AbstractCallbackTest {
    private static final long CASE_ID = 12345L;
    private static final long ASYNC_METHOD_CALL_TIMEOUT = 10000;
    private static final String FAMILY_MAN_NUMBER = "FMN1";
    private static final String NOTIFICATION_REFERENCE = "localhost/" + CASE_ID;
    private static final UUID LETTER_1_ID = randomUUID();
    private static final UUID LETTER_2_ID = randomUUID();
    private static final Document NOTICE_OF_HEARING_DOCUMENT = testDocument();
    private static final Document COVERSHEET_REPRESENTATIVE = testDocument();
    private static final Document COVERSHEET_RESPONDENT = testDocument();
    private static final DocumentReference NOTICE_OF_HEARING = testDocumentReference();
    private static final DocumentReference SDO_DOCUMENT = testDocumentReference();
    private static final DocumentReference SEALED_DOCUMENT = testDocumentReference();
    private static final DocumentReference C6_DOCUMENT = testDocumentReference("notice_of_proceedings_c6.pdf");
    private static final DocumentReference C6A_DOCUMENT = testDocumentReference("notice_of_proceedings_c6a.pdf");
    private static final byte[] NOTICE_OF_HEARING_BINARY = testDocumentBinary();
    private static final byte[] COVERSHEET_REPRESENTATIVE_BINARY = testDocumentBinary();
    private static final byte[] COVERSHEET_RESPONDENT_BINARY = testDocumentBinary();
    private static final byte[] DOCUMENT_PDF_BINARIES = readBytes("documents/document1.pdf");
    private static final byte[] APPLICATION_BINARY = DOCUMENT_CONTENT;
    private static final LocalDateTime START_DATE = LocalDateTime.of(2050, 5, 20, 13, 0);
    private static final LocalDateTime END_DATE = LocalDateTime.of(2050, 5, 20, 14, 0);
    private static final DocmosisDocument DOCMOSIS_PDF_DOCUMENT = testDocmosisDocument(DOCUMENT_PDF_BINARIES)
        .toBuilder().documentTitle("pdf.pdf").build();
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
        // TODO: add this to the only test
        when(translationRequestFormCreationService.buildTranslationRequestDocuments(any()))
            .thenReturn(DOCMOSIS_PDF_DOCUMENT);
        when(documentDownloadService.downloadDocument(any())).thenReturn(APPLICATION_BINARY);
        when(docmosisHelper.extractPdfContent(APPLICATION_BINARY)).thenReturn("Some content");
        when(sealingService.sealDocument(eq(SDO_DOCUMENT), any(), any())).thenReturn(SEALED_DOCUMENT);
    }

    @Test
    void shouldTriggerSendNoticeOfHearingEventForNewHearingWhenNoticeOfHearingPresent()
        throws NotificationClientException {
        final CaseData cdb = caseDataWithSDOAndNopAndNohToTranslateBefore;
        final CaseData cd = caseDataWithSDOAndNopAndNohToTranslateAfter;

        givenFplService();

        given(documentDownloadService.downloadDocument(NOTICE_OF_HEARING.getBinaryUrl()))
            .willReturn(NOTICE_OF_HEARING_BINARY);

        given(documentConversionService.convertToPdf(NOTICE_OF_HEARING))
            .willReturn(NOTICE_OF_HEARING);

        given(otherRecipientsInbox.getNonSelectedRecipients(
            EMAIL,
            cdb,
            emptyList(),
            element -> element.getValue().getEmail())
        ).willReturn(emptySet());

        given(sendLetterApi.sendLetter(eq(SERVICE_AUTH_TOKEN), any(LetterWithPdfsRequest.class)))
            .willReturn(new SendLetterResponse(LETTER_1_ID))
            .willReturn(new SendLetterResponse(LETTER_2_ID));

        given(uploadDocumentService.uploadPDF(NOTICE_OF_HEARING_BINARY, NOTICE_OF_HEARING.getFilename()))
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

        verify(notificationClient, timeout(ASYNC_METHOD_CALL_TIMEOUT)).sendEmail(
            eq(SDO_AND_NOP_ISSUED_CAFCASS),
            eq(CAFCASS_EMAIL),
            anyMap(),
            eq(NOTIFICATION_REFERENCE));

        verify(notificationClient, timeout(ASYNC_METHOD_CALL_TIMEOUT)).sendEmail(
            eq(SDO_AND_NOP_ISSUED_LA),
            eq(LOCAL_AUTHORITY_3_INBOX),
            anyMap(),
            eq(NOTIFICATION_REFERENCE));

        verify(notificationClient).sendEmail(
            eq(SDO_AND_NOP_ISSUED_CTSC),
            eq(DEFAULT_CTSC_EMAIL),
            anyMap(),
            eq(NOTIFICATION_REFERENCE));

        verifyEmailSentToTranslation(3);

        verify(sendLetterApi, timeout(ASYNC_METHOD_CALL_TIMEOUT).times(2)).sendLetter(
            eq(SERVICE_AUTH_TOKEN),
            printRequestCaptor.capture());

        verify(coreCaseDataService, timeout(ASYNC_METHOD_CALL_TIMEOUT)).updateCase(
            eq(CASE_ID), caseCaptor.capture());

        LetterWithPdfsRequest expectedPrintRequest1 = printRequest(CASE_ID, NOTICE_OF_HEARING,
            COVERSHEET_REPRESENTATIVE_BINARY, NOTICE_OF_HEARING_BINARY);

        LetterWithPdfsRequest expectedPrintRequest2 = printRequest(CASE_ID, NOTICE_OF_HEARING,
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
//        verifyNoMoreNotificationsSent();

//        verifyNoMoreInteractions(coreCaseDataService);
    }

    private void verifyEmailSentToTranslation(int timesCalled) {
        checkUntil(() -> verify(emailService, times(timesCalled)).sendEmail(eq("sender@example.com"), any()));
    }

    private void verifyNoMoreNotificationsSent() {
        checkThat(() -> verifyNoMoreInteractions(notificationClient, emailService), Duration.ofSeconds(2));
    }

    final Element<HearingBooking> hearingWithNotice = element(HearingBooking.builder()
        .type(CASE_MANAGEMENT)
        .startDate(START_DATE)
        .endDate(END_DATE)
        .noticeOfHearing(NOTICE_OF_HEARING)
        .venue("96")
        .build());

    private final CaseData caseDataWithSDOAndNopAndNohToTranslateBefore = CaseData.builder()
        .id(CASE_ID)
        .familyManCaseNumber(FAMILY_MAN_NUMBER)
        .caseLocalAuthority(LOCAL_AUTHORITY_3_CODE)
        .hearingDetails(null)
        .state(GATEKEEPING)
        .respondents1(wrapElements(RESPONDENT_REPRESENTED, RESPONDENT_NOT_REPRESENTED))
        .representatives(List.of(REPRESENTATIVE_DIGITAL,
            REPRESENTATIVE_EMAIL, REPRESENTATIVE_POST))
        .gatekeepingOrderRouter(GatekeepingOrderRoute.SERVICE)
        .standardDirectionOrder(StandardDirectionOrder.builder()
            .orderStatus(SEALED)
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

    private final CaseData caseDataWithSDOAndNopAndNohToTranslateAfter =
        caseDataWithSDOAndNopAndNohToTranslateBefore.toBuilder()
            .hearingDetails(List.of(hearingWithNotice))
            .selectedHearingId(hearingWithNotice.getId())
            .state(GATEKEEPING)
            .build();
}
