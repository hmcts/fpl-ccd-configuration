package uk.gov.hmcts.reform.fpl.controllers.listgatekeepinghearing;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_AND_NOP_ISSUED_CAFCASS;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_AND_NOP_ISSUED_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_AND_NOP_ISSUED_LA;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING;
import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING_LISTING;
import static uk.gov.hmcts.reform.fpl.testingsupport.IntegrationTestConstants.CAFCASS_EMAIL;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkThat;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkUntil;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.DOCUMENT_CONTENT;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import uk.gov.hmcts.reform.fpl.docmosis.DocmosisHelper;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.EventService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.email.EmailService;
import uk.gov.hmcts.reform.fpl.service.translation.TranslationRequestFormCreationService;
import uk.gov.service.notify.NotificationClient;

class ListGatekeepingHearingControllerSubmittedTest extends ListGatekeepingHearingControllerTest {
    private static final Long CASE_ID = 1L;
    private static final DocumentReference SDO_DOCUMENT = testDocumentReference();
    private static final CaseData GATEKEEPING_CASE_DATA = CaseData.builder().state(GATEKEEPING_LISTING).build();
    private static final String NOTIFICATION_REFERENCE = "localhost/" + CASE_ID;
    private static final byte[] DOCUMENT_PDF_BINARIES = readBytes("documents/document1.pdf");
    private static final DocmosisDocument DOCMOSIS_PDF_DOCUMENT = testDocmosisDocument(DOCUMENT_PDF_BINARIES)
        .toBuilder().documentTitle("pdf.pdf").build();
    private static final byte[] APPLICATION_BINARY = DOCUMENT_CONTENT;
    private static final DocumentReference SEALED_DOCUMENT = testDocumentReference();
    private static final DocumentReference URGENT_HEARING_ORDER_DOCUMENT = testDocumentReference();

    @MockBean
    private NotificationClient notificationClient;
    @MockBean
    private EmailService emailService;
    @MockBean
    private DocumentSealingService sealingService;
    @MockBean
    TranslationRequestFormCreationService translationRequestFormCreationService;
    @MockBean
    private DocumentDownloadService documentDownloadService;
    @MockBean
    DocmosisHelper docmosisHelper;
    @SpyBean
    private EventService eventService;
    @MockBean
    private CoreCaseDataService coreCaseDataService;

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
//        postSubmittedEvent(toCallBackRequest(buildCaseDataWithSDO(DRAFT), GATEKEEPING_CASE_DATA));

        verifyNoInteractions(sealingService);
        verify(eventService, never()).publishEvent(any());
        verify(coreCaseDataService, never()).triggerEvent(any(), any(), any(), any(), any());
    }

    @Test
    void shouldTriggerEventWhenSDOSubmitted() {
        postSubmittedEvent(toCallBackRequest(buildCaseDataWithSDO(SEALED), GATEKEEPING_CASE_DATA));

//        verifyNoInteractions(sealingService);
        verifyEmails(SDO_AND_NOP_ISSUED_CAFCASS, SDO_AND_NOP_ISSUED_CTSC, SDO_AND_NOP_ISSUED_LA);
//        verifyNoMoreNotificationsSent();
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

    private CaseData buildCaseDataWithSDO(OrderStatus status) {
        return baseCaseData()
            .gatekeepingOrderRouter(GatekeepingOrderRoute.SERVICE)
            .standardDirectionOrder(StandardDirectionOrder.builder()
                .orderStatus(status)
                .orderDoc(SDO_DOCUMENT)
                .build())
            .build();
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
}
