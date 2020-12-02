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

import static java.util.Collections.singletonList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_REMOVAL_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.GATEKEEPER_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class StandardDirectionsOrderRemovedEventHandlerTest {

    private static final Long CASE_ID = 12345L;
    private static final String FAKE_URL = "https://fake.url";

    @Mock
    private NotificationService notificationService;

    @Mock
    private OrderRemovalEmailContentProvider contentProvider;

    @InjectMocks
    private StandardDirectionsOrderRemovedEventHandler eventHandler;

    @Test
    void shouldSendEmailToGatekeepersWhenStandardDirectionOrderAddedToCase() {
        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .gatekeeperEmails(singletonList(element(EmailAddress.builder().email(GATEKEEPER_EMAIL_ADDRESS).build())))
            .build();

        String removalReason = "test removal reason";

        OrderRemovalTemplate expectedTemplate = expectedTemplate(removalReason);
        given(contentProvider.buildNotificationForOrderRemoval(caseData, removalReason))
            .willReturn(expectedTemplate);

        eventHandler.notifyGatekeeperOfRemovedSDO(new StandardDirectionsOrderRemovedEvent(caseData, removalReason));

        verify(notificationService).sendEmail(
            SDO_REMOVAL_NOTIFICATION_TEMPLATE,
            singletonList(GATEKEEPER_EMAIL_ADDRESS),
            expectedTemplate,
            String.valueOf(CASE_ID));
    }

    private OrderRemovalTemplate expectedTemplate(String removalReason) {
        return OrderRemovalTemplate.builder()
            .respondentLastName("Smith")
            .removalReason(removalReason)
            .caseReference(String.valueOf(CASE_ID))
            .caseUrl(FAKE_URL)
            .build();
    }
}
