package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.StandardDirectionsOrderRemovedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.notify.orderremoval.OrderRemovalTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.OrderRemovalEmailContentProvider;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_REMOVAL_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StandardDirectionsOrderRemovedEventHandler {

    private final NotificationService notificationService;
    private final OrderRemovalEmailContentProvider orderRemovalEmailContentProvider;

    @EventListener
    public void notifyGatekeeperOfRemovedSDO(StandardDirectionsOrderRemovedEvent event) {
        CaseData caseData = event.getCaseData();

        OrderRemovalTemplate template =
            orderRemovalEmailContentProvider.buildNotificationForOrderRemoval(caseData, event.getRemovalReason());

        List<String> emailList = getDistinctGatekeeperEmails(caseData.getGatekeeperEmails());

        notificationService.sendEmail(
            SDO_REMOVAL_NOTIFICATION_TEMPLATE, emailList, template, String.valueOf(caseData.getId())
        );
    }

    private List<String> getDistinctGatekeeperEmails(List<Element<EmailAddress>> emailCollection) {
        return unwrapElements(emailCollection)
            .stream()
            .distinct()
            .map(EmailAddress::getEmail)
            .collect(Collectors.toList());
    }
}
