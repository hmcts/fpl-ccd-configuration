package uk.gov.hmcts.reform.fpl.service.email;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.notify.BaseCaseNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.service.email.NotificationServiceTest.ENV;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {NotificationService.class, JacksonAutoConfiguration.class})
@TestPropertySource(properties = {"fpl.env=" + ENV})
class NotificationServiceTest {

    static final String ENV = "TEST_ENV";
    private static final String TEST_RECIPIENT_EMAIL_1 = "test1@example.com";
    private static final String TEST_RECIPIENT_EMAIL_2 = "test2@example.com";
    private static final String REFERENCE = "12345L";
    private static final String TEMPLATE_ID = "some template id";
    private static final String NOTIFICATION_REFERENCE = String.format("%s/%s", ENV, REFERENCE);

    @MockBean
    private NotificationClient notificationClient;

    @Autowired
    private NotificationService notificationService;

    private static final NotifyData EMAIL_PERSONALISATION = BaseCaseNotifyData.builder()
        .lastName("Smith")
        .caseUrl("http://fake-url")
        .build();

    private static final Map<String, Object> EXPECTED_EMAIL_PERSONALISATION = Map.of(
        "respondentLastName", "Smith",
        "caseUrl", "http://fake-url");

    @Test
    void shouldSendEmailToSingleRecipient() throws NotificationClientException {
        notificationService.sendEmail(TEMPLATE_ID, TEST_RECIPIENT_EMAIL_1, EMAIL_PERSONALISATION, REFERENCE);

        verify(notificationClient).sendEmail(
            TEMPLATE_ID,
            TEST_RECIPIENT_EMAIL_1,
            EXPECTED_EMAIL_PERSONALISATION,
            NOTIFICATION_REFERENCE
        );
    }

    @Test
    void shouldSendEmailsToMultipleRecipients() throws NotificationClientException {
        notificationService.sendEmail(TEMPLATE_ID, Set.of(TEST_RECIPIENT_EMAIL_1, TEST_RECIPIENT_EMAIL_2),
            EMAIL_PERSONALISATION, REFERENCE);

        verify(notificationClient).sendEmail(
            TEMPLATE_ID,
            TEST_RECIPIENT_EMAIL_1,
            EXPECTED_EMAIL_PERSONALISATION,
            NOTIFICATION_REFERENCE
        );

        verify(notificationClient).sendEmail(
            TEMPLATE_ID,
            TEST_RECIPIENT_EMAIL_2,
            EXPECTED_EMAIL_PERSONALISATION,
            NOTIFICATION_REFERENCE
        );
    }

}
