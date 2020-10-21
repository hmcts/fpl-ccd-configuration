package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.service.notify.NotificationClient;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ALLOCATED_JUDGE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ActiveProfiles("integration-test")
@WebMvcTest(AllocatedJudgeController.class)
@OverrideAutoConfiguration(enabled = true)
public class AllocatedJudgeControllerSubmittedTest extends AbstractControllerTest {
    private static final String ALLOCATED_JUDGE_EMAIL = "judge@gmail.com";
    private static final String NOTIFICATION_REFERENCE = "localhost/12345";

    @MockBean
    private NotificationClient notificationClient;

    AllocatedJudgeControllerSubmittedTest() {
        super("allocated-judge");
    }

    @Test
    void shouldNotifyAllocatedJudgeWhenAssignedToACase() throws Exception {
        postSubmittedEvent(callbackRequest());

        verify(notificationClient).sendEmail(
            eq(ALLOCATED_JUDGE_TEMPLATE), eq(ALLOCATED_JUDGE_EMAIL),
            anyMap(), eq(NOTIFICATION_REFERENCE));
    }
}
