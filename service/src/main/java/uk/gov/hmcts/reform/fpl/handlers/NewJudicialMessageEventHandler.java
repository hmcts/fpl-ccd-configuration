package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.NewJudicialMessageEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.JudicialMessageMetaData;
import uk.gov.hmcts.reform.fpl.model.notify.NewJudicialMessageTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.NewJudicialMessageContentProvider;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NEW_JUDICIAL_MESSAGE_ADDED_TEMPLATE;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NewJudicialMessageEventHandler {
    private final NotificationService notificationService;
    private final NewJudicialMessageContentProvider newJudicialMessageContentProvider;

    @EventListener
    public void notifyJudicialMessageRecipient(NewJudicialMessageEvent event) {
        CaseData caseData = event.getCaseData();
        JudicialMessageMetaData judicialMessageMetaData = caseData.getJudicialMessageMetaData();

        NewJudicialMessageTemplate notifyData =
            newJudicialMessageContentProvider.buildNewJudicialMessageTemplate(caseData);

        notificationService.sendEmail(NEW_JUDICIAL_MESSAGE_ADDED_TEMPLATE, judicialMessageMetaData.getRecipient(),
            notifyData, caseData.getId());
    }
}
