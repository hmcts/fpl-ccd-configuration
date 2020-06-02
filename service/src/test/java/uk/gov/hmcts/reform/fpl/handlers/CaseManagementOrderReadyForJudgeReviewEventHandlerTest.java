package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderReadyForJudgeReviewEvent;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForCMO;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.AllocatedJudgeNotificationType.CMO;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.ALLOCATED_JUDGE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CTSC_INBOX;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.appendSendToCtscOnCallback;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.getCMOReadyForJudgeNotificationParameters;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {CaseManagementOrderReadyForJudgeReviewEventHandler.class, JacksonAutoConfiguration.class,
    LookupTestConfig.class, HmctsAdminNotificationHandler.class})
public class CaseManagementOrderReadyForJudgeReviewEventHandlerTest {
    private static final String CASE_REFERENCE = "12345";

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private CaseManagementOrderEmailContentProvider caseManagementOrderEmailContentProvider;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private CaseManagementOrderReadyForJudgeReviewEventHandler caseManagementOrderReadyForJudgeReviewEventHandler;

    @Test
    void shouldNotifyHmctsAdminOfCMOReadyForJudgeReviewWhenCtscIsDisabled() {
        CallbackRequest callbackRequest = callbackRequest();
        CaseDetails caseDetails = callbackRequest().getCaseDetails();
        AllocatedJudgeTemplateForCMO cmoJudgeReviewParameters = getCMOReadyForJudgeNotificationParameters();

        given(caseManagementOrderEmailContentProvider
            .buildCMOReadyForJudgeReviewNotificationParameters(caseDetails))
            .willReturn(cmoJudgeReviewParameters);

        caseManagementOrderReadyForJudgeReviewEventHandler.sendEmailForCaseManagementOrderReadyForJudgeReview(
            new CaseManagementOrderReadyForJudgeReviewEvent(callbackRequest));

        verify(notificationService).sendEmail(
            CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE,
            COURT_EMAIL_ADDRESS,
            cmoJudgeReviewParameters,
            CASE_REFERENCE);
    }

    @Test
    void shouldNotifyCtscAdminOfCMOReadyForJudgeReviewWhenCtscIsEnabled() throws Exception {
        CallbackRequest callbackRequest = appendSendToCtscOnCallback();
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        AllocatedJudgeTemplateForCMO cmoJudgeReviewParameters = getCMOReadyForJudgeNotificationParameters();

        given(caseManagementOrderEmailContentProvider
            .buildCMOReadyForJudgeReviewNotificationParameters(caseDetails))
            .willReturn(cmoJudgeReviewParameters);

        caseManagementOrderReadyForJudgeReviewEventHandler.sendEmailForCaseManagementOrderReadyForJudgeReview(
            new CaseManagementOrderReadyForJudgeReviewEvent(callbackRequest));

        verify(notificationService).sendEmail(
            CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE,
            CTSC_INBOX,
            cmoJudgeReviewParameters,
            CASE_REFERENCE);
    }

    @Test
    void shouldNotifyAllocatedJudgeWhenCMOReadyForJudgeReviewAndEnabled() {
        CallbackRequest callbackRequest = callbackRequest();
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        AllocatedJudgeTemplateForCMO cmoJudgeReviewParameters = getCMOReadyForJudgeNotificationParameters();

        given(featureToggleService.isAllocatedJudgeNotificationEnabled(CMO)).willReturn(true);

        given(caseManagementOrderEmailContentProvider
            .buildCMOReadyForJudgeReviewNotificationParameters(caseDetails))
            .willReturn(cmoJudgeReviewParameters);

        caseManagementOrderReadyForJudgeReviewEventHandler
            .sendEmailForCaseManagementOrderReadyForJudgeReviewToAllocatedJudge(
            new CaseManagementOrderReadyForJudgeReviewEvent(callbackRequest));

        verify(notificationService).sendEmail(
            CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE_JUDGE,
            ALLOCATED_JUDGE_EMAIL_ADDRESS,
            cmoJudgeReviewParameters,
            CASE_REFERENCE);
    }

    @Test
    void shouldNotNotifyAllocatedJudgeWhenCMOReadyForJudgeReviewAndDisabled() {
        CallbackRequest callbackRequest = callbackRequest();
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        AllocatedJudgeTemplateForCMO cmoJudgeReviewParameters = getCMOReadyForJudgeNotificationParameters();

        given(featureToggleService.isAllocatedJudgeNotificationEnabled(CMO)).willReturn(false);

        given(caseManagementOrderEmailContentProvider
            .buildCMOReadyForJudgeReviewNotificationParameters(caseDetails))
            .willReturn(cmoJudgeReviewParameters);

        caseManagementOrderReadyForJudgeReviewEventHandler
            .sendEmailForCaseManagementOrderReadyForJudgeReviewToAllocatedJudge(
                new CaseManagementOrderReadyForJudgeReviewEvent(callbackRequest));

        verify(notificationService, never()).sendEmail(
            eq(CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE_JUDGE),
            anyString(),
            anyMap(),
            anyString());
    }

    @Test
    void shouldNotNotifyAllocatedJudgeWhenCMOReadyForJudgeReviewButNoAllocatedJudgeExists() {
        CaseDetails caseDetails =  CaseDetails.builder().id(1L)
            .data(Map.of("caseLocalAuthority", "SA"))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(
            caseDetails).build();

        AllocatedJudgeTemplateForCMO cmoJudgeReviewParameters = getCMOReadyForJudgeNotificationParameters();

        given(caseManagementOrderEmailContentProvider
            .buildCMOReadyForJudgeReviewNotificationParameters(caseDetails))
            .willReturn(cmoJudgeReviewParameters);

        caseManagementOrderReadyForJudgeReviewEventHandler
            .sendEmailForCaseManagementOrderReadyForJudgeReviewToAllocatedJudge(
                new CaseManagementOrderReadyForJudgeReviewEvent(callbackRequest));

        verify(notificationService, never()).sendEmail(
            eq(CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE_JUDGE),
            any(),
            anyMap(),
            any());
    }
}
