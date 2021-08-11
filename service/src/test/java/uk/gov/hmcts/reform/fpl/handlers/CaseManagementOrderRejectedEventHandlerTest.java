package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.events.cmo.CaseManagementOrderRejectedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.RejectedCMOTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CaseManagementOrderEmailContentProvider;

import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_REJECTED_BY_JUDGE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_EMAIL_ADDRESS;

@ExtendWith(MockitoExtension.class)
class CaseManagementOrderRejectedEventHandlerTest {

    private static final long CASE_ID = 12345L;
    @Mock
    private InboxLookupService inboxLookupService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private CaseManagementOrderEmailContentProvider contentProvider;

    @InjectMocks
    private CaseManagementOrderRejectedEventHandler underTest;

    @Test
    void shouldNotifyLocalAuthorityOfCMORejected() {
        CaseData caseData = mock(CaseData.class);
        HearingOrder cmo = mock(HearingOrder.class);
        RejectedCMOTemplate notiftyData = mock(RejectedCMOTemplate.class);

        given(inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build()))
            .willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        given(contentProvider.buildCMORejectedByJudgeNotificationParameters(caseData, cmo))
            .willReturn(notiftyData);

        given(caseData.getId()).willReturn(CASE_ID);

        underTest.notifyLocalAuthority(new CaseManagementOrderRejectedEvent(caseData, cmo));

        verify(notificationService).sendEmail(
            CMO_REJECTED_BY_JUDGE_TEMPLATE, Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS), notiftyData,
            String.valueOf(CASE_ID)
        );
    }
}
