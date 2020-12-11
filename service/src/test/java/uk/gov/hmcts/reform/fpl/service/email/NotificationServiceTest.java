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
import uk.gov.service.notify.SendEmailResponse;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.service.email.NotificationServiceTest.ENV;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {NotificationService.class, JacksonAutoConfiguration.class})
@TestPropertySource(properties = {"fpl.env=" + ENV})
class NotificationServiceTest {
    private static final String TEST_RECIPIENT_EMAIL = "test@example.com";
    private static final String REFERENCE = "12345L";
    private static final String TEMPLATE_ID = PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE;
    static final String ENV = "TEST_ENV";
    private static final String NOTIFICATION_REFERENCE = String.format("%s/%s", ENV, REFERENCE);
    private static final SendEmailResponse EMAIL_RESPONSE = mock(SendEmailResponse.class);

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private NoOpNotificationResponsePostProcessor notificationResponsePostProcessor;

    @Autowired
    private NotificationService notificationService;

    @Test
    void shouldSendNotificationSuccessfullyWhenDataValid() throws NotificationClientException {
        NotifyData notifyData = BaseCaseNotifyData.builder()
            .respondentLastName("Smith")
            .caseUrl("http://fake-url")
            .build();

        Map<String, Object> expectedParams = Map.of(
            "respondentLastName", "Smith",
            "caseUrl", "http://fake-url");

        when(notificationClient.sendEmail(
            PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE,
            TEST_RECIPIENT_EMAIL,
            expectedParams,
            NOTIFICATION_REFERENCE)).thenReturn(EMAIL_RESPONSE);

        notificationService.sendEmail(TEMPLATE_ID, TEST_RECIPIENT_EMAIL, notifyData, REFERENCE);

        verify(notificationResponsePostProcessor).process(EMAIL_RESPONSE);

    }

}
