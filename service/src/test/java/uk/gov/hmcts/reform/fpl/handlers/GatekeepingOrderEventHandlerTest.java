package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.GatekeepingOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.sdo.SDONotifyData;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.SDOIssuedCafcassContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.SDOIssuedContentProvider;

import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_AND_NOP_ISSUED_CAFCASS;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_AND_NOP_ISSUED_LA;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.URGENT_AND_NOP_ISSUED_CTSC;
import static uk.gov.hmcts.reform.fpl.enums.notification.GatekeepingOrderNotificationGroup.SDO_AND_NOP;
import static uk.gov.hmcts.reform.fpl.enums.notification.GatekeepingOrderNotificationGroup.URGENT_AND_NOP;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_NAME;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CTSC_INBOX;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;

@ExtendWith(MockitoExtension.class)
class GatekeepingOrderEventHandlerTest {
    private static final Long CASE_ID = 12345L;
    private static final SDONotifyData NOTIFY_DATA = mock(SDONotifyData.class);
    private static final DocumentReference ORDER = mock(DocumentReference.class);
    private static final GatekeepingOrderEvent.GatekeepingOrderEventBuilder BASE_EVENT = GatekeepingOrderEvent.builder()
        .order(ORDER)
        .notificationGroup(SDO_AND_NOP);

    @Mock
    private NotificationService notificationService;
    @Mock
    private InboxLookupService inboxLookupService;
    @Mock
    private CafcassLookupConfiguration cafcassLookup;
    @Mock
    private CtscEmailLookupConfiguration ctscLookup;
    @Mock
    private SDOIssuedContentProvider standardContentProvider;
    @Mock
    private SDOIssuedCafcassContentProvider cafcassContentProvider;

    @InjectMocks
    private GatekeepingOrderEventHandler underTest;

    @Test
    void shouldNotifyCafcassOfIssuedSDO() {
        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .build();

        given(cafcassLookup.getCafcass(LOCAL_AUTHORITY_CODE))
            .willReturn(new CafcassLookupConfiguration.Cafcass(CAFCASS_NAME, CAFCASS_EMAIL_ADDRESS));

        given(cafcassContentProvider.getNotifyData(caseData, ORDER)).willReturn(NOTIFY_DATA);

        underTest.notifyCafcass(BASE_EVENT.caseData(caseData).build());

        verify(notificationService).sendEmail(
            SDO_AND_NOP_ISSUED_CAFCASS,
            CAFCASS_EMAIL_ADDRESS,
            NOTIFY_DATA,
            CASE_ID
        );
    }

    @Test
    void shouldNotifyLocalAuthorityOfIssuedSDO() {
        final CaseData caseData = CaseData.builder().id(CASE_ID).build();

        given(standardContentProvider.buildNotificationParameters(caseData)).willReturn(NOTIFY_DATA);

        given(inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder()
                .caseData(caseData)
                .build())
        ).willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        underTest.notifyLocalAuthority(BASE_EVENT.caseData(caseData).build());

        verify(notificationService).sendEmail(
            SDO_AND_NOP_ISSUED_LA,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            NOTIFY_DATA,
            CASE_ID.toString()
        );
    }

    @Test
    void shouldNotifyCTSCOfIssuedSDO() {
        final CaseData caseData = CaseData.builder().id(CASE_ID).build();

        given(standardContentProvider.buildNotificationParameters(caseData)).willReturn(NOTIFY_DATA);

        given(ctscLookup.getEmail()).willReturn(CTSC_INBOX);

        underTest.notifyCTSC(BASE_EVENT.caseData(caseData).notificationGroup(URGENT_AND_NOP).build());

        verify(notificationService).sendEmail(
            URGENT_AND_NOP_ISSUED_CTSC,
            CTSC_INBOX,
            NOTIFY_DATA,
            CASE_ID
        );
    }
}
