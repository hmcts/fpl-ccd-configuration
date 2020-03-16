package uk.gov.hmcts.reform.fpl.service.email;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientApi;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE;

@ExtendWith(SpringExtension.class)
public class NotificationServiceTest {
    public static final String TEST_RECIPIENT_EMAIL = "test@example.com";
    public static final String REFERENCE = "12345L";
    public static final String TEMPLATE_ID = PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE;

    @Mock
    private NotificationClientApi notificationClientApi;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void shouldSendNotificationSuccessfullyWhenDataValid() throws NotificationClientException {
        Map<String, Object> templatePreference = getDefaultForPartyAddedToCaseByEmailTemplate();

        notificationService.sendEmail(TEMPLATE_ID, TEST_RECIPIENT_EMAIL, templatePreference, REFERENCE);

        verify(notificationClient).sendEmail(eq(PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE),
            eq(TEST_RECIPIENT_EMAIL), eq(templatePreference), eq(REFERENCE));
    }

    @Test
    void shouldNotSendEmailWhenExpectedRecipientEmailIsNull() throws NotificationClientException {
        // See -> https://docs.notifications.service.gov.uk/java.html#send-an-email-arguments-personalisation-required
        Map<String, Object> templatePreference = new HashMap<>();

        notificationService.sendEmail(TEMPLATE_ID, null, templatePreference, REFERENCE);

        verify(notificationClientApi, never()).sendEmail(TEMPLATE_ID, null, templatePreference, REFERENCE);
    }

    private static Map<String, Object> getDefaultForPartyAddedToCaseByEmailTemplate() {
        return ImmutableMap.<String, Object>builder()
            .put("firstRespondentLastName", "John Snow")
            .put("familyManCaseNumber", UUID.randomUUID())
            .build();
    }
}
