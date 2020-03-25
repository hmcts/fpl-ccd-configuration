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
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderRejectedEvent;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.HmctsEmailContentProvider;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_REJECTED_BY_JUDGE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.AUTH_TOKEN;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.USER_ID;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.getCMORejectedCaseLinkNotificationParameters;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {CaseManagementOrderRejectedEventHandler.class, JacksonAutoConfiguration.class,
    LookupTestConfig.class, HmctsEmailContentProvider.class})
public class CaseManagementOrderRejectedEventHandlerTest {
    @MockBean
    private InboxLookupService inboxLookupService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private CaseManagementOrderEmailContentProvider caseManagementOrderEmailContentProvider;

    @Autowired
    private CaseManagementOrderRejectedEventHandler caseManagementOrderRejectedEventHandler;

    @Test
    void shouldNotifyLocalAuthorityOfCMORejected() throws Exception {
        CallbackRequest callbackRequest = callbackRequest();
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        given(inboxLookupService.getNotificationRecipientEmail(caseDetails, LOCAL_AUTHORITY_CODE))
            .willReturn(LOCAL_AUTHORITY_EMAIL_ADDRESS);

        given(caseManagementOrderEmailContentProvider.buildCMORejectedByJudgeNotificationParameters(caseDetails))
            .willReturn(getCMORejectedCaseLinkNotificationParameters());

        caseManagementOrderRejectedEventHandler.notifyLocalAuthorityOfRejectedCaseManagementOrder(
            new CaseManagementOrderRejectedEvent(callbackRequest, AUTH_TOKEN, USER_ID));

        verify(notificationService).sendEmail(
            CMO_REJECTED_BY_JUDGE_TEMPLATE,
            LOCAL_AUTHORITY_EMAIL_ADDRESS,
            getCMORejectedCaseLinkNotificationParameters(),
            "12345");
    }
}
