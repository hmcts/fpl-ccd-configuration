package uk.gov.hmcts.reform.fpl.service.email;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {NotificationService.class, NotificationClient.class, JacksonAutoConfiguration.class})
@TestPropertySource(properties = {"fpl.env=localhost" })
public class NotificationServiceTest {
    public static final String TEST_RECIPIENT_EMAIL = "test@example.com";
    public static final String REFERENCE = "12345L";
    public static final String TEMPLATE_ID = PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE;
    public static final String ENVIRONMENT = "localhost";

    @MockBean
    private NotificationClient notificationClient;

    @Autowired
    private NotificationService notificationService;

    @Test
    void shouldSendNotificationSuccessfullyWhenDataValid() throws NotificationClientException {
        Map<String, Object> templatePreference = getDefaultForPartyAddedToCaseByEmailTemplate();

        notificationService.sendEmail(TEMPLATE_ID, TEST_RECIPIENT_EMAIL, templatePreference, REFERENCE);

        verify(notificationClient).sendEmail(eq(PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE),
            eq(TEST_RECIPIENT_EMAIL), eq(templatePreference), eq(ENVIRONMENT + "/" + REFERENCE));
    }

    private static Map<String, Object> getDefaultForPartyAddedToCaseByEmailTemplate() {
        return ImmutableMap.<String, Object>builder()
            .put("firstRespondentLastName", "John Snow")
            .put("familyManCaseNumber", UUID.randomUUID())
            .build();
    }
}
