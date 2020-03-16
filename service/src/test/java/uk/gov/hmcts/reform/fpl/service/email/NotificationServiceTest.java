package uk.gov.hmcts.reform.fpl.service.email;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE;

@ExtendWith(SpringExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationClient notificationClient;

    private NotificationService notificationService;

    @BeforeEach
    void setup() {
        notificationService = new NotificationService(notificationClient);
    }

    @Test
    void shouldSendNotificationSuccessfullyWhenDataValid() throws NotificationClientException {
        Map<String, Object> templatePreference =
            TestNotificationPreferenceData.getDefaultForPartyAddedToCaseByEmailTemplate();

        notificationService.sendNotification(PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE,
            "test@example.com", templatePreference, "12345L");

        verify(notificationClient).sendEmail(anyString(), anyString(), anyMap(), anyString());
    }

    @Test
    void shouldNotSendNotificationWhenExpectedParametersIsNull() throws NotificationClientException {
        notificationService.sendNotification(null, null, null, null);

        verify(notificationClient, never()).sendEmail(anyString(), anyString(), anyMap(), anyString());
    }

    static class TestNotificationPreferenceData {
        static Map<String, Object> getDefaultForPartyAddedToCaseByEmailTemplate() {
            return ImmutableMap.<String, Object>builder()
                .put("firstRespondentLastName", "John Snow")
                .put("familyManCaseNumber", UUID.randomUUID())
                .build();
        }
    }
}
