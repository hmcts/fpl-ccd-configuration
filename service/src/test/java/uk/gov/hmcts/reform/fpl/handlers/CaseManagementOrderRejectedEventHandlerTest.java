package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderRejectedEvent;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_REJECTED_BY_JUDGE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.getCMORejectedCaseLinkNotificationParameters;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ExtendWith(SpringExtension.class)
public class CaseManagementOrderRejectedEventHandlerTest {

    @Mock
    private InboxLookupService inboxLookupService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private CaseManagementOrderEmailContentProvider caseManagementOrderEmailContentProvider;

    @InjectMocks
    private CaseManagementOrderRejectedEventHandler caseManagementOrderRejectedEventHandler;

    @Test
    void shouldNotifyLocalAuthorityOfCMORejected() {
        CallbackRequest callbackRequest = callbackRequest();
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        given(inboxLookupService.getNotificationRecipientEmail(caseDetails, LOCAL_AUTHORITY_CODE))
            .willReturn(LOCAL_AUTHORITY_EMAIL_ADDRESS);

        given(caseManagementOrderEmailContentProvider.buildCMORejectedByJudgeNotificationParameters(caseDetails))
            .willReturn(getCMORejectedCaseLinkNotificationParameters());

        caseManagementOrderRejectedEventHandler.notifyLocalAuthorityOfRejectedCaseManagementOrder(
            new CaseManagementOrderRejectedEvent(callbackRequest));

        verify(notificationService).sendEmail(
            CMO_REJECTED_BY_JUDGE_TEMPLATE,
            LOCAL_AUTHORITY_EMAIL_ADDRESS,
            getCMORejectedCaseLinkNotificationParameters(),
            caseDetails.getId().toString());
    }
}
