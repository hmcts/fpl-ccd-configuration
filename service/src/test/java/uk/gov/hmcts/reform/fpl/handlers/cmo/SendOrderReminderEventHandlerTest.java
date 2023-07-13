package uk.gov.hmcts.reform.fpl.handlers.cmo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.events.cmo.SendOrderReminderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.ChaseMissingCMOsTemplate;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.ChaseMissingCMOEmailContentProvider;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CHASE_OUTSTANDING_ORDER_LA_TEMPLATE;

@ExtendWith(MockitoExtension.class)
class SendOrderReminderEventHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private ChaseMissingCMOEmailContentProvider contentProvider;

    @Mock
    private LocalAuthorityRecipientsService localAuthorityRecipients;

    @InjectMocks
    private SendOrderReminderEventHandler underTest;

    private static final ChaseMissingCMOsTemplate TEMPLATE_DATA = ChaseMissingCMOsTemplate.builder().build();
    private static final Set<String> RECIPIENTS = Set.of("la@test.com");
    private static final long CASE_ID = 12345L;

    @Test
    void shouldSendNotificationOnEvent() {
        when(contentProvider.buildTemplate(any())).thenReturn(TEMPLATE_DATA);
        when(localAuthorityRecipients.getRecipients(any())).thenReturn(RECIPIENTS);
        final CaseData caseData = CaseData.builder().id(CASE_ID).build();
        final SendOrderReminderEvent event = new SendOrderReminderEvent(caseData);

        underTest.sendNotificationToApplicant(event);

        verify(notificationService).sendEmail(CHASE_OUTSTANDING_ORDER_LA_TEMPLATE, RECIPIENTS, TEMPLATE_DATA, CASE_ID);
    }

}
