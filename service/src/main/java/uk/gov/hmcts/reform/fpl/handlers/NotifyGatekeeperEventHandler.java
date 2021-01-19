package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.NotifyGatekeepersEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.SharedNotifyTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.sendtogatekeeper.NotifyGatekeeperTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.GatekeeperEmailContentProvider;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.GATEKEEPER_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.getDistinctGatekeeperEmails;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotifyGatekeeperEventHandler {
    private final NotificationService notificationService;
    private final GatekeeperEmailContentProvider gatekeeperEmailContentProvider;

    @EventListener
    public void notifyGatekeeper(NotifyGatekeepersEvent event) {
        CaseData caseData = event.getCaseData();

        NotifyGatekeeperTemplate parameters = gatekeeperEmailContentProvider.buildGatekeeperNotification(caseData);

        List<String> emailList = getDistinctGatekeeperEmails(caseData.getGatekeeperEmails());

        emailList.forEach(recipientEmail -> {
            SharedNotifyTemplate template = parameters.duplicate();
            notificationService.sendEmail(GATEKEEPER_SUBMISSION_TEMPLATE, recipientEmail, template, caseData.getId());
        });
    }
}
