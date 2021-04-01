package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import uk.gov.hmcts.reform.fpl.events.NotifyGatekeepersEvent;
import uk.gov.hmcts.reform.fpl.service.EventService;
import uk.gov.service.notify.NotificationClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.GATEKEEPER_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@WebMvcTest(NotifyGatekeeperController.class)
@OverrideAutoConfiguration(enabled = true)
class NotifyGatekeeperControllerSubmittedTest extends AbstractCallbackTest {
    private static final String GATEKEEPER_EMAIL = "FamilyPublicLaw+gatekeeper@gmail.com";
    private static final String CAFCASS_EMAIL = "Cafcass+gatekeeper@gmail.com";
    private static final String NOTIFICATION_REFERENCE = "localhost/12345";

    @MockBean
    private NotificationClient notificationClient;

    @SpyBean
    private EventService eventPublisher;

    NotifyGatekeeperControllerSubmittedTest() {
        super("notify-gatekeeper");
    }

    @Test
    void shouldNotifyMultipleGatekeepers() throws Exception {
        postSubmittedEvent(callbackRequest());

        verify(notificationClient).sendEmail(
            eq(GATEKEEPER_SUBMISSION_TEMPLATE), eq(GATEKEEPER_EMAIL),
            anyMap(), eq(NOTIFICATION_REFERENCE));

        verify(notificationClient).sendEmail(
            eq(GATEKEEPER_SUBMISSION_TEMPLATE), eq(CAFCASS_EMAIL),
            anyMap(), eq(NOTIFICATION_REFERENCE));

        verify(eventPublisher).publishEvent(any(NotifyGatekeepersEvent.class));
        verifyNoMoreInteractions(eventPublisher);
    }
}
