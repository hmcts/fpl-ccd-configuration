package uk.gov.hmcts.reform.fpl.handlers.cmo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.cmo.CMORemovedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.orderremoval.OrderRemovalTemplate;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderRemovalEmailContentProvider;

import java.util.Collection;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CMO_REMOVAL_NOTIFICATION_TEMPLATE;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CMORemovedEventHandler {
    private final LocalAuthorityRecipientsService localAuthorityRecipients;
    private final NotificationService notificationService;
    private final OrderRemovalEmailContentProvider orderRemovalEmailContentProvider;

    @EventListener
    public void notifyLocalAuthorityOfRemovedCMO(CMORemovedEvent event) {
        CaseData caseData = event.getCaseData();

        OrderRemovalTemplate template =
            orderRemovalEmailContentProvider.buildNotificationForOrderRemoval(caseData, event.getRemovalReason());

        final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
            .caseData(caseData)
            .secondaryLocalAuthorityExcluded(true)
            .build();

        final Collection<String> recipients = localAuthorityRecipients.getRecipients(recipientsRequest);

        notificationService.sendEmail(CMO_REMOVAL_NOTIFICATION_TEMPLATE, recipients, template, caseData.getId());
    }
}
