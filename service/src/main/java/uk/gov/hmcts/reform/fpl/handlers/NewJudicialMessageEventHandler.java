package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.NewJudicialMessageEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.notify.NewJudicialMessageTemplate;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.JudicialMessageContentProvider;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.JUDICIAL_MESSAGE_ADDED_TEMPLATE;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NewJudicialMessageEventHandler {
    private final NotificationService notificationService;
    private final JudicialMessageContentProvider newJudicialMessageContentProvider;
    private final FeatureToggleService featureToggleService;

    @EventListener
    public void notifyJudicialMessageRecipient(NewJudicialMessageEvent event) {
        if (featureToggleService.isWATaskEmailsEnabled()) {
            CaseData caseData = event.getCaseData();
            JudicialMessage newJudicialMessage = event.getJudicialMessage();

            NewJudicialMessageTemplate notifyData =
                newJudicialMessageContentProvider.buildNewJudicialMessageTemplate(caseData, newJudicialMessage);

            notificationService.sendEmail(JUDICIAL_MESSAGE_ADDED_TEMPLATE, newJudicialMessage.getRecipient(),
                notifyData, caseData.getId());
        } else {
            log.info("WA EMAIL SKIPPED - new judicial message - {}", event.getCaseData().getId());
        }
    }
}
