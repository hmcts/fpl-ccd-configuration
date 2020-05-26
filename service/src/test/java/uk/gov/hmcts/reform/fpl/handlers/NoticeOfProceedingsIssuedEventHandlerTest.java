package uk.gov.hmcts.reform.fpl.handlers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.events.NoticeOfProceedingsIssuedEvent;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.NoticeOfProceedingsEmailContentProvider;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_PROCEEDINGS_ISSUED_JUDGE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.ALLOCATED_JUDGE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {NoticeOfProceedingsIssuedEventHandler.class, JacksonAutoConfiguration.class,
    LookupTestConfig.class})
public class NoticeOfProceedingsIssuedEventHandlerTest {
    private static CallbackRequest callbackRequest = callbackRequest();

    @MockBean
    private RequestData requestData;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NoticeOfProceedingsEmailContentProvider noticeOfProceedingsEmailContentProvider;

    @Autowired
    private NoticeOfProceedingsIssuedEventHandler noticeOfProceedingsIssuedEventHandler;

    @Test
    void shouldNotifyAllocatedJudgeOfIssuedNoticeOfProceedingsWhenNotificationEnabled() {
        final Map<String, Object> expectedParameters = getAllocatedJudgeSDOTemplateParameters();

        given(featureToggleService.isNoticeOfProceedingsAllocatedJudgeNotificationsEnabled()).willReturn(true);

        given(noticeOfProceedingsEmailContentProvider.buildAllocatedJudgeNotification(
            callbackRequest.getCaseDetails())).willReturn(expectedParameters);

        noticeOfProceedingsIssuedEventHandler.notifyAllocatedJudgeOfIssuedStandardDirectionsOrder(
            new NoticeOfProceedingsIssuedEvent(callbackRequest, requestData));

        verify(notificationService).sendEmail(
            NOTICE_OF_PROCEEDINGS_ISSUED_JUDGE_TEMPLATE, ALLOCATED_JUDGE_EMAIL_ADDRESS, expectedParameters,
            "12345");
    }

    @Test
    void shouldNotNotifyAllocatedJudgeOfIssuedNoticeOfProceedingsWhenNotificationDisabled() {
        final Map<String, Object> expectedParameters = getAllocatedJudgeSDOTemplateParameters();

        given(featureToggleService.isNoticeOfProceedingsAllocatedJudgeNotificationsEnabled()).willReturn(false);

        given(noticeOfProceedingsEmailContentProvider.buildAllocatedJudgeNotification(
            callbackRequest.getCaseDetails())).willReturn(expectedParameters);

        noticeOfProceedingsIssuedEventHandler.notifyAllocatedJudgeOfIssuedStandardDirectionsOrder(
            new NoticeOfProceedingsIssuedEvent(callbackRequest, requestData));

        verify(notificationService, never()).sendEmail(any(), any(), anyMap(), any());
    }

    private Map<String, Object> getAllocatedJudgeSDOTemplateParameters() {
        return ImmutableMap.<String, Object>builder()
            .put("familyManCaseNumber", "6789")
            .put("leadRespondentsName", "Moley")
            .put("hearingDate", "21 October 2020")
            .put("judgeTitle", "Her Honour Judge")
            .put("judgeName", "Byrne")
            .put("caseUrl", "null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();
    }
}
