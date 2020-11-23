package uk.gov.hmcts.reform.fpl.handlers.cmo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.events.cmo.CMORemovedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.sdo.OrderRemovalTemplate;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderRemovalEmailContentProvider;

import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_REMOVAL_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.caseData;

@ExtendWith(MockitoExtension.class)
class CMORemovedEventHandlerTest {

    static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "FamilyPublicLaw+sa@gmail.com";
    private static final Long CASE_ID = 12345L;
    private static final String FAKE_URL = "https://fake.url";

    @Mock
    private InboxLookupService inboxLookupService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private OrderRemovalEmailContentProvider contentProvider;

    @InjectMocks
    private CMORemovedEventHandler eventHandler;

    @Test
    void shouldSendEmailToPartiesWhenAddedToCase() {
        CaseData caseData = caseData();
        String removalReason = "removal reason details";

        OrderRemovalTemplate expectedTemplate = expectedTemplate(removalReason);

        given(inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build())
        ).willReturn(Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        given(contentProvider.buildNotificationForOrderRemoval(caseData, removalReason))
            .willReturn(expectedTemplate);

        eventHandler.notifyLocalAuthorityOfRemovedCMO(new CMORemovedEvent(caseData, removalReason));

        verify(notificationService).sendEmail(
            CMO_REMOVAL_NOTIFICATION_TEMPLATE,
            Set.of(LOCAL_AUTHORITY_EMAIL_ADDRESS),
            expectedTemplate,
            String.valueOf(CASE_ID)
        );
    }

    private OrderRemovalTemplate expectedTemplate(String removalReason) {
        OrderRemovalTemplate template = new OrderRemovalTemplate();
        template.setRespondentLastName("Smith");
        template.setReturnedNote(removalReason);
        template.setCaseReference(String.valueOf(CASE_ID));
        template.setCaseUrl(FAKE_URL);
        return template;
    }
}
