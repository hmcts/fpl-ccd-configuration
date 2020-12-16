package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.NewJudicialMessageEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.notify.NewJudicialMessageTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.JudicialMessageContentProvider;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.JUDICIAL_MESSAGE_ADDED_TEMPLATE;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NewJudicialMessageEventHandler {
    private final NotificationService notificationService;
    private final JudicialMessageContentProvider newJudicialMessageContentProvider;

    @EventListener
    public void notifyJudicialMessageRecipient(NewJudicialMessageEvent event) {
        CaseData caseData = event.getCaseData();
        JudicialMessage newJudicialMessage = event.getJudicialMessage();

        NewJudicialMessageTemplate notifyData =
            newJudicialMessageContentProvider.buildNewJudicialMessageTemplate(caseData, newJudicialMessage);

        notificationService.sendEmail(JUDICIAL_MESSAGE_ADDED_TEMPLATE, newJudicialMessage.getRecipient(),
            notifyData, caseData.getId());
    }
}
