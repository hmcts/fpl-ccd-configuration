package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.events.NotifyGatekeepersEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.notify.sendtogatekeeper.NotifyGatekeeperTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.GatekeeperEmailContentProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.GATEKEEPER_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.GATEKEEPER_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class NotifyGatekeeperEventHandlerTest {
    private static final Long CASE_ID = 12345L;
    private static final String EMAIL_1 = GATEKEEPER_EMAIL_ADDRESS;
    private static final String EMAIL_2 = "Cafcass+gatekeeper@gmail.com";
    private static final NotifyGatekeeperTemplate BASE_NOTIFY_DATA = mock(NotifyGatekeeperTemplate.class);
    private static final NotifyGatekeeperTemplate DUPLICATE_1 = mock(NotifyGatekeeperTemplate.class);
    private static final NotifyGatekeeperTemplate DUPLICATE_2 = mock(NotifyGatekeeperTemplate.class);

    @Captor
    private ArgumentCaptor<NotifyGatekeeperTemplate> captor;
    @Mock
    private NotificationService notificationService;
    @Mock
    private GatekeeperEmailContentProvider contentProvider;
    @InjectMocks
    private NotifyGatekeeperEventHandler underTest;

    @Test
    void shouldSendEmailToMultipleGatekeepers() {
        CaseData caseData = mock(CaseData.class);

        when(caseData.getGatekeeperEmails()).thenReturn(wrapElements(
            EmailAddress.builder().email(EMAIL_1).build(),
            EmailAddress.builder().email(EMAIL_2).build()
        ));

        when(caseData.getId()).thenReturn(CASE_ID);

        when(contentProvider.buildGatekeeperNotification(caseData)).thenReturn(BASE_NOTIFY_DATA);
        when(BASE_NOTIFY_DATA.duplicate()).thenReturn(DUPLICATE_1, DUPLICATE_2);

        underTest.notifyGatekeeper(new NotifyGatekeepersEvent(caseData));

        verify(notificationService).sendEmail(
            eq(GATEKEEPER_SUBMISSION_TEMPLATE), eq(EMAIL_1), captor.capture(), eq(CASE_ID)
        );

        verify(notificationService).sendEmail(
            eq(GATEKEEPER_SUBMISSION_TEMPLATE), eq(EMAIL_2), captor.capture(), eq(CASE_ID)
        );

        assertThat(captor.getAllValues()).containsExactly(DUPLICATE_1, DUPLICATE_2);
    }
}
