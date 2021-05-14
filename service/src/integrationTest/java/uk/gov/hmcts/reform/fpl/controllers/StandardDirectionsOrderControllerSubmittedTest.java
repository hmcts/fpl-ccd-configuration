package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.events.GatekeepingOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.EventService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_AND_NOP_ISSUED_CAFCASS;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_AND_NOP_ISSUED_CTSC;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.DOCUMENT_CONTENT;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(StandardDirectionsOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class StandardDirectionsOrderControllerSubmittedTest extends AbstractCallbackTest {
    private static final Long CASE_ID = 1L;
    private static final String SEND_DOCUMENT_EVENT = "internal-change-SEND_DOCUMENT";
    private static final DocumentReference SDO_DOCUMENT = testDocumentReference();
    private static final DocumentReference URGENT_HEARING_ORDER_DOCUMENT = testDocumentReference();
    private static final String NOTIFICATION_REFERENCE = "localhost/" + CASE_ID;
    private static final byte[] APPLICATION_BINARY = DOCUMENT_CONTENT;

    @SpyBean
    private EventService eventService;

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    StandardDirectionsOrderControllerSubmittedTest() {
        super("draft-standard-directions");
    }

    @BeforeEach
    void init() {
        when(documentDownloadService.downloadDocument(any())).thenReturn(APPLICATION_BINARY);
    }

    @Test
    void shouldNotTriggerSDOEventWhenDraft() {
        postSubmittedEvent(buildCaseDataWithSDO(DRAFT));

        verify(eventService, never()).publishEvent(GatekeepingOrderEvent.class);
    }

    @Test
    void shouldNotTriggerSendDocumentEventWhenDraft() {
        postSubmittedEvent(buildCaseDataWithSDO(DRAFT));

        verify(coreCaseDataService, never()).triggerEvent(any(), any(), any(), eq(SEND_DOCUMENT_EVENT), any());
    }

    @Test
    void shouldTriggerEventWhenUrgentHearingSubmitted() throws NotificationClientException {
        postSubmittedEvent(buildCaseDataWithUrgentHearingOrder());

        verify(notificationClient).sendEmail(
            eq(SDO_AND_NOP_ISSUED_CAFCASS),
            eq("cafcass@cafcass.com"),
            anyMap(),
            eq(NOTIFICATION_REFERENCE)
        );

        verify(notificationClient).sendEmail(
            eq(SDO_AND_NOP_ISSUED_CTSC),
            eq("FamilyPublicLaw+ctsc@gmail.com"),
            anyMap(),
            eq(NOTIFICATION_REFERENCE)
        );
    }

    @Test
    void shouldTriggerSDOEventWhenSubmitted() throws NotificationClientException {
        postSubmittedEvent(buildCaseDataWithSDO(SEALED));

        verify(notificationClient).sendEmail(
            eq(SDO_AND_NOP_ISSUED_CAFCASS),
            eq("cafcass@cafcass.com"),
            anyMap(),
            eq(NOTIFICATION_REFERENCE)
        );

        verify(notificationClient).sendEmail(
            eq(SDO_AND_NOP_ISSUED_CTSC),
            eq("FamilyPublicLaw+ctsc@gmail.com"),
            anyMap(),
            eq(NOTIFICATION_REFERENCE)
        );
    }

    @Test
    void shouldTriggerSendDocumentEventWhenSubmitted() {
        postSubmittedEvent(buildCaseDataWithSDO(SEALED));

        verify(coreCaseDataService).triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            SEND_DOCUMENT_EVENT,
            Map.of("documentToBeSent", SDO_DOCUMENT)
        );
    }

    private CaseData buildCaseDataWithSDO(OrderStatus status) {
        return baseCaseData()
            .standardDirectionOrder(StandardDirectionOrder.builder()
                .orderStatus(status)
                .orderDoc(SDO_DOCUMENT)
                .build())
            .build();
    }

    private CaseData buildCaseDataWithUrgentHearingOrder() {
        return baseCaseData()
            .urgentHearingOrder(UrgentHearingOrder.builder()
                .order(URGENT_HEARING_ORDER_DOCUMENT)
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
