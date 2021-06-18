package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.events.StandardDirectionsOrderRemovedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.notify.orderremoval.OrderRemovalTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderRemovalEmailContentProvider;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_REMOVAL_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.GATEKEEPER_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class StandardDirectionsOrderRemovedEventHandlerTest {

    private static final Long CASE_ID = 12345L;

    @Mock
    private NotificationService notificationService;

    @Mock
    private OrderRemovalEmailContentProvider contentProvider;

    @InjectMocks
    private StandardDirectionsOrderRemovedEventHandler eventHandler;

    @Test
    void shouldSendEmailToGatekeepersWhenStandardDirectionOrderRemovedFromCase() {
        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .gatekeeperEmails(wrapElements(EmailAddress.builder().email(GATEKEEPER_EMAIL_ADDRESS).build()))
            .build();
        final OrderRemovalTemplate notifyData = mock(OrderRemovalTemplate.class);

        String removalReason = "test removal reason";

        given(contentProvider.buildNotificationForOrderRemoval(caseData, removalReason))
            .willReturn(notifyData);

        eventHandler.notifyGatekeeperOfRemovedSDO(new StandardDirectionsOrderRemovedEvent(caseData, removalReason));

        verify(notificationService).sendEmail(
            SDO_REMOVAL_NOTIFICATION_TEMPLATE, List.of(GATEKEEPER_EMAIL_ADDRESS), notifyData, String.valueOf(CASE_ID)
        );
    }
}
