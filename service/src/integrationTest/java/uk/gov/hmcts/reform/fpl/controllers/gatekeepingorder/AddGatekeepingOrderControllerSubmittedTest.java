package uk.gov.hmcts.reform.fpl.controllers.gatekeepingorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.AddGatekeepingOrderController;
import uk.gov.hmcts.reform.fpl.docmosis.DocmosisHelper;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.EventService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.email.EmailService;
import uk.gov.hmcts.reform.fpl.service.translation.TranslationRequestFormCreationService;
import uk.gov.service.notify.NotificationClient;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_AND_NOP_ISSUED_CAFCASS;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_AND_NOP_ISSUED_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_AND_NOP_ISSUED_LA;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_ISSUED_CAFCASS;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_ISSUED_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_ISSUED_LA;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.URGENT_AND_NOP_ISSUED_CAFCASS;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.URGENT_AND_NOP_ISSUED_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.URGENT_AND_NOP_ISSUED_LA;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.WELSH_TO_ENGLISH;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.State.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkThat;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkUntil;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.DOCUMENT_CONTENT;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

//Exactly the same as StandardDirectionsOrderControllerSubmittedTest
@WebMvcTest(AddGatekeepingOrderController.class)
@OverrideAutoConfiguration(enabled = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AddGatekeepingOrderControllerSubmittedTest extends AbstractCallbackTest {
    private static final Long CASE_ID = 1L;
    private static final String SEND_DOCUMENT_EVENT = "internal-change-SEND_DOCUMENT";
    private static final DocumentReference SDO_DOCUMENT = testDocumentReference();
    private static final DocumentReference C6_DOCUMENT = testDocumentReference("notice_of_proceedings_c6.pdf");
    private static final DocumentReference C6A_DOCUMENT = testDocumentReference("notice_of_proceedings_c6a.pdf");
    private static final DocumentReference URGENT_HEARING_ORDER_DOCUMENT = testDocumentReference();
    private static final byte[] DOCUMENT_PDF_BINARIES = readBytes("documents/document1.pdf");
    private static final DocmosisDocument DOCMOSIS_PDF_DOCUMENT = testDocmosisDocument(DOCUMENT_PDF_BINARIES)
        .toBuilder().documentTitle("pdf.pdf").build();
    private static final LocalDate DATE_ADDED = LocalDate.of(2018, 2, 4);

    private static final String NOTIFICATION_REFERENCE = "localhost/" + CASE_ID;
    private static final byte[] APPLICATION_BINARY = DOCUMENT_CONTENT;
    private static final CaseData GATEKEEPING_CASE_DATA = CaseData.builder().state(GATEKEEPING).build();
    private static final CaseData CASE_MANAGEMENT_CASE_DATA = CaseData.builder().state(CASE_MANAGEMENT).build();

    @MockBean
    TranslationRequestFormCreationService translationRequestFormCreationService;

    @SpyBean
    private EventService eventService;

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private EmailService emailService;

    @MockBean
    DocmosisHelper docmosisHelper;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    AddGatekeepingOrderControllerSubmittedTest() {
        super("add-gatekeeping-order");
    }

    @BeforeEach
    void init() {
        when(translationRequestFormCreationService.buildTranslationRequestDocuments(any()))
            .thenReturn(DOCMOSIS_PDF_DOCUMENT);
        when(documentDownloadService.downloadDocument(any())).thenReturn(APPLICATION_BINARY);
        when(docmosisHelper.extractPdfContent(APPLICATION_BINARY)).thenReturn("Some content");
    }

    @Test
    void shouldNotTriggerEventsWhenDraft() {
        postSubmittedEvent(toCallBackRequest(buildCaseDataWithSDO(DRAFT), GATEKEEPING_CASE_DATA));

        verify(eventService, never()).publishEvent(any());
        verify(coreCaseDataService, never()).triggerEvent(any(), any(), any(), any(), any());
    }

    @Test
    void shouldNotTriggerEventsWhenDraftAfterUrgentHearingOrder() {
        postSubmittedEvent(toCallBackRequest(
            buildCaseDataWithUrgentHearingOrderAndSDO(DRAFT), CASE_MANAGEMENT_CASE_DATA
        ));

        verify(eventService, never()).publishEvent(any());
        verify(coreCaseDataService, never()).triggerEvent(any(), any(), any(), any(), any());
    }

    @Test
    void shouldTriggerEventWhenUrgentHearingSubmitted() {
        postSubmittedEvent(toCallBackRequest(buildCaseDataWithUrgentHearingOrder(), GATEKEEPING_CASE_DATA));

        verifyEmails(URGENT_AND_NOP_ISSUED_CAFCASS, URGENT_AND_NOP_ISSUED_CTSC, URGENT_AND_NOP_ISSUED_LA);
        verifyNoMoreNotificationsSent();
    }

    @Test
    void shouldTriggerEventWhenUrgentHearingSubmittedAndRequestingTranslation() {
        postSubmittedEvent(toCallBackRequest(buildCaseDataWithUrgentHearingOrderToTranslate(), GATEKEEPING_CASE_DATA));

        verifyEmails(URGENT_AND_NOP_ISSUED_CAFCASS, URGENT_AND_NOP_ISSUED_CTSC, URGENT_AND_NOP_ISSUED_LA);
        verifyEmailSentToTranslation();
        verifyNoMoreNotificationsSent();
    }

    @Test
    void shouldTriggerEventWhenUrgentWithNoPHearingSubmittedAndRequestingTranslation() {
        postSubmittedEvent(toCallBackRequest(buildCaseDataWithUrgentHearingOrderToTranslateWithNop(),
            GATEKEEPING_CASE_DATA));

        verifyEmails(URGENT_AND_NOP_ISSUED_CAFCASS, URGENT_AND_NOP_ISSUED_CTSC, URGENT_AND_NOP_ISSUED_LA);
        verifyEmailSentToTranslation(3);
        verifyNoMoreNotificationsSent();
    }

    @Test
    void shouldTriggerEventWhenSDOSubmitted() {
        postSubmittedEvent(toCallBackRequest(buildCaseDataWithSDO(SEALED), GATEKEEPING_CASE_DATA));

        verifyEmails(SDO_AND_NOP_ISSUED_CAFCASS, SDO_AND_NOP_ISSUED_CTSC, SDO_AND_NOP_ISSUED_LA);
        verifyNoMoreNotificationsSent();
    }

    @Test
    void shouldTriggerEventWhenSDOSubmittedWithTranslation() {
        postSubmittedEvent(toCallBackRequest(buildCaseDataWithSDOToTranslate(SEALED), GATEKEEPING_CASE_DATA));

        verifyEmails(SDO_AND_NOP_ISSUED_CAFCASS, SDO_AND_NOP_ISSUED_CTSC, SDO_AND_NOP_ISSUED_LA);
        verifyEmailSentToTranslation();
        verifyNoMoreNotificationsSent();
    }

    @Test
    void shouldTriggerEventWhenSDOAndNoticeOfProceedingsSubmittedWithTranslation() {
        postSubmittedEvent(toCallBackRequest(buildCaseDataWithSDOAndNopToTranslate(SEALED), GATEKEEPING_CASE_DATA));

        verifyEmails(SDO_AND_NOP_ISSUED_CAFCASS, SDO_AND_NOP_ISSUED_CTSC, SDO_AND_NOP_ISSUED_LA);
        verifyEmailSentToTranslation(3);
        verifyNoMoreNotificationsSent();
    }

    @Test
    void shouldTriggerEventWhenSDOSubmittedAfterUrgentHearingOrder() {
        postSubmittedEvent(toCallBackRequest(
            buildCaseDataWithUrgentHearingOrderAndSDO(SEALED), CASE_MANAGEMENT_CASE_DATA
        ));

        verifyEmails(SDO_ISSUED_CAFCASS, SDO_ISSUED_CTSC, SDO_ISSUED_LA);
        verifyNoMoreNotificationsSent();
    }

    @Test
    void shouldTriggerEventWhenSDOSubmittedAfterUrgentHearingOrderWithTranslation() {
        postSubmittedEvent(toCallBackRequest(
            buildCaseDataWithUrgentHearingOrderAndSDOToTranslate(SEALED), CASE_MANAGEMENT_CASE_DATA
        ));

        verifyEmails(SDO_ISSUED_CAFCASS, SDO_ISSUED_CTSC, SDO_ISSUED_LA);
        verifyEmailSentToTranslation();
        verifyNoMoreNotificationsSent();
    }

    @Test
    void shouldTriggerSendDocumentEventWhenSubmitted() {
        postSubmittedEvent(toCallBackRequest(buildCaseDataWithSDO(SEALED), GATEKEEPING_CASE_DATA));

        verify(coreCaseDataService).triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            SEND_DOCUMENT_EVENT,
            Map.of("documentToBeSent", SDO_DOCUMENT)
        );
    }

    @Test
    void shouldTriggerSendDocumentEventForUrgentHearingOrder() {
        postSubmittedEvent(toCallBackRequest(buildCaseDataWithUrgentHearingOrder(), GATEKEEPING_CASE_DATA));

        verify(coreCaseDataService).triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            SEND_DOCUMENT_EVENT,
            Map.of("documentToBeSent", URGENT_HEARING_ORDER_DOCUMENT)
        );
    }

    private void verifyEmails(String cafcassTemplate, String ctcsTemplate, String laTemplate) {
        checkUntil(() -> verify(notificationClient).sendEmail(
            eq(cafcassTemplate),
            eq("cafcass@cafcass.com"),
            anyMap(),
            eq(NOTIFICATION_REFERENCE)
        ));

        checkUntil(() -> verify(notificationClient).sendEmail(
            eq(ctcsTemplate),
            eq("FamilyPublicLaw+ctsc@gmail.com"),
            anyMap(),
            eq(NOTIFICATION_REFERENCE)
        ));

        checkUntil(() -> verify(notificationClient).sendEmail(
            eq(laTemplate),
            eq("shared@test1.org.uk"),
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

    private CaseData buildCaseDataWithUrgentHearingOrderAndSDO(OrderStatus status) {
        return buildCaseDataWithUrgentHearingOrder().toBuilder()
            .standardDirectionOrder(StandardDirectionOrder.builder()
                .orderStatus(status)
                .orderDoc(SDO_DOCUMENT)
                .build())
            .build();
    }

    private CaseData buildCaseDataWithUrgentHearingOrderAndSDOToTranslate(OrderStatus status) {
        return buildCaseDataWithUrgentHearingOrderToTranslate().toBuilder()
            .standardDirectionOrder(StandardDirectionOrder.builder()
                .orderStatus(status)
                .orderDoc(SDO_DOCUMENT)
                .translationRequirements(WELSH_TO_ENGLISH)
                .build())
            .build();
    }

    private CaseData buildCaseDataWithSDO(OrderStatus status) {
        return baseCaseData()
            .standardDirectionOrder(StandardDirectionOrder.builder()
                .orderStatus(status)
                .orderDoc(SDO_DOCUMENT)
                .build())
            .build();
    }

    private CaseData buildCaseDataWithSDOToTranslate(OrderStatus status) {
        return baseCaseData()
            .standardDirectionOrder(StandardDirectionOrder.builder()
                .orderStatus(status)
                .orderDoc(SDO_DOCUMENT)
                .translationRequirements(WELSH_TO_ENGLISH)
                .build())
            .build();
    }

    private CaseData buildCaseDataWithSDOAndNopToTranslate(OrderStatus status) {
        return baseCaseData()
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

    private CaseData buildCaseDataWithUrgentHearingOrder() {
        return baseCaseData()
            .urgentHearingOrder(UrgentHearingOrder.builder()
                .order(URGENT_HEARING_ORDER_DOCUMENT)
                .dateAdded(DATE_ADDED)
                .build())
            .build();
    }

    private CaseData buildCaseDataWithUrgentHearingOrderToTranslate() {
        return baseCaseData()
            .urgentHearingOrder(UrgentHearingOrder.builder()
                .order(URGENT_HEARING_ORDER_DOCUMENT)
                .dateAdded(DATE_ADDED)
                .translationRequirements(WELSH_TO_ENGLISH)
                .build())
            .build();
    }

    private CaseData buildCaseDataWithUrgentHearingOrderToTranslateWithNop() {
        return baseCaseData()
            .noticeOfProceedingsBundle(List.of(
                element(DocumentBundle.builder()
                    .document(C6_DOCUMENT)
                    .translationRequirements(WELSH_TO_ENGLISH)
                    .build()),
                element(DocumentBundle.builder()
                    .document(C6A_DOCUMENT)
                    .translationRequirements(WELSH_TO_ENGLISH)
                    .build())
            )).urgentHearingOrder(UrgentHearingOrder.builder()
                .order(URGENT_HEARING_ORDER_DOCUMENT)
                .dateAdded(DATE_ADDED)
                .translationRequirements(WELSH_TO_ENGLISH)
                .build())
            .build();
    }

    private CaseData.CaseDataBuilder baseCaseData() {
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
}
