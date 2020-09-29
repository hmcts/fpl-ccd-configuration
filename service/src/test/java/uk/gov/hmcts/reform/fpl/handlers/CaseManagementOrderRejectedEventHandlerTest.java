package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderRejectedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.RejectedCMOTemplate;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;

import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_REJECTED_BY_JUDGE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;

@ExtendWith(SpringExtension.class)
class CaseManagementOrderRejectedEventHandlerTest {

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
        CaseData caseData = caseData();
        CaseManagementOrder cmo = CaseManagementOrder.builder().build();

        RejectedCMOTemplate expectedTemplate = new RejectedCMOTemplate();

        given(inboxLookupService.getRecipients(caseData))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        given(caseManagementOrderEmailContentProvider.buildCMORejectedByJudgeNotificationParameters(caseData, cmo))
            .willReturn(expectedTemplate);

        caseManagementOrderRejectedEventHandler.notifyLocalAuthorityOfRejectedCaseManagementOrder(
            new CaseManagementOrderRejectedEvent(caseData, cmo));

        verify(notificationService).sendEmail(
            CMO_REJECTED_BY_JUDGE_TEMPLATE,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            expectedTemplate,
            caseData.getId().toString());
    }
}
