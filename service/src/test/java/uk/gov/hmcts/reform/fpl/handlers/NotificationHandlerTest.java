package uk.gov.hmcts.reform.fpl.handlers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.service.EmailLookUpService;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ExtendWith(SpringExtension.class)
class NotificationHandlerTest {

    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String EMAIL_ADDRESS = "FamilyPublicLaw+test@gmail.com";
    private static final String ADDITIONAL_EMAIL_ADDRESS = "FamilyPublicLaw+test2@gmail.com";
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "1";
    private static final String CASE_ID = "12345";
    private static final String TEMPLATE_ID = "1b1be684-9b0a-4e58-8e51-f0c3c2dba37c";


    @Mock
    private EmailLookUpService emailLookUpService;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private NotificationHandler notificationHandler;

    @Test
    void shouldSendEmail() throws IOException {
        CallbackRequest request = callbackRequest();

        given(emailLookUpService.getEmails(LOCAL_AUTHORITY_CODE))
            .willReturn(ImmutableList.<String>builder().add(EMAIL_ADDRESS).build());

        notificationHandler.sendNotificationToHmctsAdmin(new SubmittedCaseEvent(request, AUTH_TOKEN, USER_ID));

        verify(notificationClient, times(1));
    }

    @Test
    void shouldBeAbleToSendMultipleEmails() throws IOException {
        CallbackRequest request = callbackRequest();

        given(emailLookUpService.getEmails(LOCAL_AUTHORITY_CODE))
            .willReturn(
                ImmutableList.<String>builder()
                    .add(EMAIL_ADDRESS, ADDITIONAL_EMAIL_ADDRESS)
                    .build());

        notificationHandler.sendNotificationToHmctsAdmin(new SubmittedCaseEvent(request, AUTH_TOKEN, USER_ID));

        verify(notificationClient, times(2));
    }

    @Test
    void notificationClientThrowingExceptionDoesNotStopFurtherEmails() throws IOException, NotificationClientException {
        Map<String, String> parameters = ImmutableMap.<String, String>builder()
            .put("court", "")
            .put("localAuthority", "")
            .put("orders", "[Emergency protection order]")
            .put("directionsAndInterim", "Information on the whereabouts of the child")
            .put("timeFramePresent", "Yes")
            .put("timeFrame", "Same day")
            .put("reference", CASE_ID)
            .put("caseUrl", "http://www.webAddress/" + CASE_ID)
            .build();

        given(notificationClient.sendEmail(
            TEMPLATE_ID, EMAIL_ADDRESS, parameters, CASE_ID)).willThrow(NotificationClientException.class);

        CallbackRequest request = callbackRequest();

        given(emailLookUpService.getEmails(LOCAL_AUTHORITY_CODE))
            .willReturn(
                ImmutableList.<String>builder()
                    .add(EMAIL_ADDRESS, ADDITIONAL_EMAIL_ADDRESS)
                    .build());

        notificationHandler.sendNotificationToHmctsAdmin(new SubmittedCaseEvent(request, AUTH_TOKEN, USER_ID));

        verify(notificationClient, times(1)).sendEmail(
            TEMPLATE_ID, ADDITIONAL_EMAIL_ADDRESS, parameters, CASE_ID);
    }
}
