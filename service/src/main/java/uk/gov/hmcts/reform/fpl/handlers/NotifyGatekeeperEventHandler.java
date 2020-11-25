package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.NotifyGatekeepersEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.notify.sendtogatekeeper.NotifyGatekeeperTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.GatekeeperEmailContentProvider;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.GATEKEEPER_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

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
            NotifyGatekeeperTemplate template = parameters.duplicate();
            notificationService.sendEmail(GATEKEEPER_SUBMISSION_TEMPLATE, recipientEmail, template, caseData.getId());
        });
    }

    private List<String> getDistinctGatekeeperEmails(List<Element<EmailAddress>> emailCollection) {
        return unwrapElements(emailCollection)
            .stream()
            .distinct()
            .map(EmailAddress::getEmail)
            .collect(Collectors.toList());
    }
}
