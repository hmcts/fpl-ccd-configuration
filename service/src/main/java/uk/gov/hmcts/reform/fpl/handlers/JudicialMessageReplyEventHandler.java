package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.JudicialMessageReplyEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.notify.JudicialMessageReplyTemplate;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.JudicialMessageReplyContentProvider;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.JUDICIAL_MESSAGE_REPLY_TEMPLATE;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class JudicialMessageReplyEventHandler {
    private final NotificationService notificationService;
    private final JudicialMessageReplyContentProvider judicialMessageReplyContentProvider;
    private final FeatureToggleService featureToggleService;
    private final CtscEmailLookupConfiguration ctscEmailLookupConfiguration;

    @EventListener
    public void notifyRecipientOfReply(JudicialMessageReplyEvent event) {
        JudicialMessage newJudicialMessage = event.getJudicialMessage();
        if (shouldSkipNotification(event)) {
            log.info("JudicialMessage - notification toggled off (court = {}, isCtsc = {})",
                event.getCaseData().getCourt().getName(),
                ctscEmailLookupConfiguration.getEmail().equals(event.getJudicialMessage().getRecipient()));
            return;
        }
        CaseData caseData = event.getCaseData();

        JudicialMessageReplyTemplate notifyData =
            judicialMessageReplyContentProvider.buildJudicialMessageReplyTemplate(caseData, newJudicialMessage);

        notificationService.sendEmail(JUDICIAL_MESSAGE_REPLY_TEMPLATE, newJudicialMessage.getRecipient(),
            notifyData, caseData.getId());
    }

    private boolean shouldSkipNotification(JudicialMessageReplyEvent event) {
        if (ctscEmailLookupConfiguration.getEmail().equals(event.getJudicialMessage().getRecipient())) {
            // check CTSC toggle
            return !featureToggleService.isWATaskEmailsEnabled();
        } else {
            // check local court/judicial toggle
            return !featureToggleService.isCourtNotificationEnabledForWa(event.getCaseData().getCourt());
        }
    }
}
