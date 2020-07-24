package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.controllers.cmo.ReviewCMOController;
import uk.gov.service.notify.NotificationClient;

@ActiveProfiles("integration-test")
@WebMvcTest(ReviewCMOController.class)
@OverrideAutoConfiguration(enabled = true)
public class ReviewCMOControllerSubmittedTest extends AbstractControllerTest {

    @MockBean
    private NotificationClient notificationClient;

    protected ReviewCMOControllerSubmittedTest() {
        super("review-cmo");
    }

    @Test
    void shouldNotSendNotificationsIfNoCMOsReadyForApproval() {
        CallbackRequest callbackRequest = CallbackRequest.builder().build();

        postSubmittedEvent(callbackRequest);
    }

    @Test
    void shouldSendCMOIssuedNotificationsIfJudgeApproves() {
        CallbackRequest callbackRequest = CallbackRequest.builder().build();

        postSubmittedEvent(callbackRequest);
    }

    @Test
    void shouldSendCMORejectedNotificationIfJudgeRequestedChanges() {
        CallbackRequest callbackRequest = CallbackRequest.builder().build();

        postSubmittedEvent(callbackRequest);
    }

}
