package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class NotificationServiceTest {

    private static final String USER_EMAIL = "hmcts-admin@example.com";
    private static final String REFERENCE = "reference";
    private static final String TEMPLATE_ID = "templateID";
    private static final Map<String, String> PARAMETERS = new HashMap<>();

    private NotificationService notificationService;

    @Mock
    private NotificationClient notificationClient;

    @BeforeEach
    void beforeEachTest() {
        notificationService = new NotificationService(notificationClient);
    }

    @Test
    void shouldSendEmail() throws NotificationClientException {
        notificationService.sendMail(USER_EMAIL, TEMPLATE_ID, PARAMETERS, REFERENCE);

        verify(notificationClient).sendEmail(eq(TEMPLATE_ID), eq(USER_EMAIL), eq(PARAMETERS), eq(REFERENCE));
    }
}
