package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_PROCEEDINGS_ISSUED_JUDGE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ActiveProfiles("integration-test")
@WebMvcTest(NoticeOfProceedingsController.class)
@OverrideAutoConfiguration(enabled = true)
class NoticeOfProceedingsSubmittedControllerTest extends AbstractControllerTest {

    private static final String ALLOCATED_JUDGE_EMAIL_ADDRESS = "judge@gmail.com";
    static final String UI_URL = "http://fake-url";

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
        given(featureToggleService.isNoticeOfProceedingsAllocatedJudgeNotificationsEnabled()).willReturn(true);

        postSubmittedEvent(callbackRequest().getCaseDetails());

        verify(notificationClient).sendEmail(
            NOTICE_OF_PROCEEDINGS_ISSUED_JUDGE_TEMPLATE,
            ALLOCATED_JUDGE_EMAIL_ADDRESS,
            expectedNoticeOfProceedingsNotificationParams(),
            "12345");
    }

    private Map<String, Object> expectedNoticeOfProceedingsNotificationParams() {
        return ImmutableMap.<String, Object>builder()
                .put("caseUrl", "http://fake-url/case/PUBLICLAW/CARE_SUPERVISION_EPO/12345")
                .put("familyManCaseNumber", "12345L,")
                .put("hearingDate", "1 January 2020")
                .put("judgeName", "Moley")
                .put("judgeTitle", "Her Honour Judge")
                .put("leadRespondentsName", "Smith")
                .build();
    }
}
