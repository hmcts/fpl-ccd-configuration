package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_PROCEEDINGS_ISSUED_JUDGE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.AllocatedJudgeNotificationType.NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ActiveProfiles("integration-test")
@WebMvcTest(NoticeOfProceedingsController.class)
@OverrideAutoConfiguration(enabled = true)
class NoticeOfProceedingsSubmittedControllerTest extends AbstractControllerTest {

    private static final String ALLOCATED_JUDGE_EMAIL_ADDRESS = "judge@gmail.com";
    private static final String CASE_ID = "12345";
    private static final String NOTIFICATION_REFERENCE = "localhost/" + CASE_ID;

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private FeatureToggleService featureToggleService;

    NoticeOfProceedingsSubmittedControllerTest() {
        super("notice-of-proceedings");
    }

    @Test
    void shouldSendAllocatedJudgeNotificationWhenNoticeOfProceedingsIssuedAndEnabled()
        throws NotificationClientException {
        given(featureToggleService.isAllocatedJudgeNotificationEnabled(NOTICE_OF_PROCEEDINGS)).willReturn(true);

        postSubmittedEvent(callbackRequest().getCaseDetails());

        verify(notificationClient).sendEmail(
            eq(NOTICE_OF_PROCEEDINGS_ISSUED_JUDGE_TEMPLATE),
            eq(ALLOCATED_JUDGE_EMAIL_ADDRESS),
            anyMap(),
            eq(NOTIFICATION_REFERENCE));
    }

    @Test
    void shouldNotSendAllocatedJudgeNotificationWhenNoticeOfProceedingsIssuedAndDisabled()
        throws NotificationClientException {
        given(featureToggleService.isAllocatedJudgeNotificationEnabled(NOTICE_OF_PROCEEDINGS)).willReturn(false);

        postSubmittedEvent(callbackRequest().getCaseDetails());

        verify(notificationClient, never()).sendEmail(
            eq(NOTICE_OF_PROCEEDINGS_ISSUED_JUDGE_TEMPLATE),
            anyString(),
            anyMap(),
            anyString());
    }
}
