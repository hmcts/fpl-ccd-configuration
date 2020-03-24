package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderReadyForJudgeReviewEvent;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.HmctsEmailContentProvider;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.AUTH_TOKEN;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CTSC_INBOX;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.USER_ID;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.appendSendToCtscOnCallback;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.getCMOReadyForJudgeNotificationParameters;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {CaseManagementOrderReadyForJudgeReviewEventHandler.class})
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, LookupTestConfig.class,
    HmctsEmailContentProvider.class})
public class CaseManagementOrderReadyForJudgeReviewEventHandlerTest {
    @MockBean
    private NotificationService notificationService;

    @MockBean
    private CaseManagementOrderEmailContentProvider caseManagementOrderEmailContentProvider;

    @Autowired
    private CaseManagementOrderReadyForJudgeReviewEventHandler caseManagementOrderReadyForJudgeReviewEventHandler;

    @Test
    void shouldNotifyHmctsAdminOfCMOReadyForJudgeReviewWhenCtscIsDisabled() throws Exception {
        CallbackRequest callbackRequest = callbackRequest();
        CaseDetails caseDetails = callbackRequest().getCaseDetails();

        given(caseManagementOrderEmailContentProvider
            .buildCMOReadyForJudgeReviewNotificationParameters(caseDetails))
            .willReturn(getCMOReadyForJudgeNotificationParameters());

        caseManagementOrderReadyForJudgeReviewEventHandler.sendEmailForCaseManagementOrderReadyForJudgeReview(
            new CaseManagementOrderReadyForJudgeReviewEvent(callbackRequest, AUTH_TOKEN, USER_ID));

        verify(notificationService).sendEmail(
            CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE,
            COURT_EMAIL_ADDRESS,
            getCMOReadyForJudgeNotificationParameters(),
            "12345");
    }

    @Test
    void shouldNotifyCtscAdminOfCMOReadyForJudgeReviewWhenCtscIsEnabled() throws Exception {
        CallbackRequest callbackRequest = appendSendToCtscOnCallback();
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        given(caseManagementOrderEmailContentProvider
            .buildCMOReadyForJudgeReviewNotificationParameters(caseDetails))
            .willReturn(getCMOReadyForJudgeNotificationParameters());

        caseManagementOrderReadyForJudgeReviewEventHandler.sendEmailForCaseManagementOrderReadyForJudgeReview(
            new CaseManagementOrderReadyForJudgeReviewEvent(callbackRequest, AUTH_TOKEN, USER_ID));

        verify(notificationService).sendEmail(
            CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE,
            CTSC_INBOX,
            getCMOReadyForJudgeNotificationParameters(),
            "12345");
    }
}
