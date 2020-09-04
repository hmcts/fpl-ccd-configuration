package uk.gov.hmcts.reform.fpl.handlers;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderReadyForJudgeReviewEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForCMO;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.AllocatedJudgeNotificationType.CMO;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.ALLOCATED_JUDGE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CTSC_INBOX;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.getCMOReadyForJudgeNotificationParameters;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {CaseManagementOrderReadyForJudgeReviewEventHandler.class, LookupTestConfig.class,
    HmctsAdminNotificationHandler.class})
class CaseManagementOrderReadyForJudgeReviewEventHandlerTest {
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
        CaseData caseData = caseData();
        AllocatedJudgeTemplateForCMO cmoJudgeReviewParameters = getCMOReadyForJudgeNotificationParameters();

        given(caseManagementOrderEmailContentProvider
            .buildCMOReadyForJudgeReviewNotificationParameters(caseData))
            .willReturn(cmoJudgeReviewParameters);

        caseManagementOrderReadyForJudgeReviewEventHandler.notifyAdmin(
            new CaseManagementOrderReadyForJudgeReviewEvent(caseData));

        verify(notificationService).sendEmail(
            CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE,
            COURT_EMAIL_ADDRESS,
            cmoJudgeReviewParameters,
            caseData.getId());
    }

    @Test
    void shouldNotifyCtscAdminOfCMOReadyForJudgeReviewWhenCtscIsEnabled() {
        CaseData caseData = CaseData.builder()
            .id(RandomUtils.nextLong())
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .sendToCtsc("Yes")
            .build();

        AllocatedJudgeTemplateForCMO cmoJudgeReviewParameters = getCMOReadyForJudgeNotificationParameters();

        given(caseManagementOrderEmailContentProvider
            .buildCMOReadyForJudgeReviewNotificationParameters(caseData))
            .willReturn(cmoJudgeReviewParameters);

        caseManagementOrderReadyForJudgeReviewEventHandler.notifyAdmin(
            new CaseManagementOrderReadyForJudgeReviewEvent(caseData));

        verify(notificationService).sendEmail(
            CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE,
            CTSC_INBOX,
            cmoJudgeReviewParameters,
            caseData.getId());
    }

    @Test
    void shouldNotifyAllocatedJudgeWhenCMOReadyForJudgeReviewAndEnabled() {
        CaseData caseData = caseData();
        AllocatedJudgeTemplateForCMO cmoJudgeReviewParameters = getCMOReadyForJudgeNotificationParameters();

        given(featureToggleService.isAllocatedJudgeNotificationEnabled(CMO)).willReturn(true);

        given(caseManagementOrderEmailContentProvider
            .buildCMOReadyForJudgeReviewNotificationParameters(caseData))
            .willReturn(cmoJudgeReviewParameters);

        caseManagementOrderReadyForJudgeReviewEventHandler
            .notifyAllocatedJudge(
                new CaseManagementOrderReadyForJudgeReviewEvent(caseData));

        verify(notificationService).sendEmail(
            CMO_READY_FOR_JUDGE_REVIEW_NOTIFICATION_TEMPLATE_JUDGE,
            ALLOCATED_JUDGE_EMAIL_ADDRESS,
            cmoJudgeReviewParameters,
            caseData.getId());
    }

    @Test
    void shouldNotNotifyAllocatedJudgeWhenCMOReadyForJudgeReviewAndDisabled() {
        CaseData caseData = caseData();
        AllocatedJudgeTemplateForCMO cmoJudgeReviewParameters = getCMOReadyForJudgeNotificationParameters();

        given(featureToggleService.isAllocatedJudgeNotificationEnabled(CMO)).willReturn(false);

        given(caseManagementOrderEmailContentProvider
            .buildCMOReadyForJudgeReviewNotificationParameters(caseData))
            .willReturn(cmoJudgeReviewParameters);

        caseManagementOrderReadyForJudgeReviewEventHandler
            .notifyAllocatedJudge(
                new CaseManagementOrderReadyForJudgeReviewEvent(caseData));

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotNotifyAllocatedJudgeWhenCMOReadyForJudgeReviewButNoAllocatedJudgeExists() {
        CaseData caseData = CaseData.builder()
            .id(1L)
            .caseLocalAuthority("SA")
            .build();

        AllocatedJudgeTemplateForCMO cmoJudgeReviewParameters = getCMOReadyForJudgeNotificationParameters();

        given(caseManagementOrderEmailContentProvider
            .buildCMOReadyForJudgeReviewNotificationParameters(caseData))
            .willReturn(cmoJudgeReviewParameters);

        caseManagementOrderReadyForJudgeReviewEventHandler
            .notifyAllocatedJudge(
                new CaseManagementOrderReadyForJudgeReviewEvent(caseData));

        verifyNoInteractions(notificationService);
    }
}
