package uk.gov.hmcts.reform.fpl.handlers.cmo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.events.cmo.CMORemovedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.orderremoval.OrderRemovalTemplate;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderRemovalEmailContentProvider;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_REMOVAL_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;

@ExtendWith(MockitoExtension.class)
class CMORemovedEventHandlerTest {

    private static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "FamilyPublicLaw+sa@gmail.com";
    private static final Long CASE_ID = 12345L;

    @Mock
    private LocalAuthorityRecipientsService localAuthorityRecipients;

    @Mock
    private NotificationService notificationService;

    @Mock
    private OrderRemovalEmailContentProvider contentProvider;

    @InjectMocks
    private CMORemovedEventHandler eventHandler;

    @Test
    void shouldSendEmailToLocalAuthorityWhenCaseManagementOrderRemovedFromCase() {
        CaseData caseData = caseData();
        String removalReason = "removal reason details";

        OrderRemovalTemplate expectedTemplate = mock(OrderRemovalTemplate.class);

        given(localAuthorityRecipients.getRecipients(any())).willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        given(contentProvider.buildNotificationForOrderRemoval(caseData, removalReason))
            .willReturn(expectedTemplate);

        eventHandler.notifyLocalAuthorityOfRemovedCMO(new CMORemovedEvent(caseData, removalReason));

        final RecipientsRequest expectedRecipientsRequest = RecipientsRequest.builder()
            .caseData(caseData)
            .secondaryLocalAuthorityExcluded(true)
            .build();

        verify(notificationService).sendEmail(
            CMO_REMOVAL_NOTIFICATION_TEMPLATE,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            expectedTemplate,
            CASE_ID
        );

        verify(localAuthorityRecipients).getRecipients(expectedRecipientsRequest);
    }
}
