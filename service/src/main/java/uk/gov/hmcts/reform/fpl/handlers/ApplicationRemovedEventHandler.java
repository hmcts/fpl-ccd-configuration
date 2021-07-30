package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.CtscTeamLeadLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.ApplicationRemovedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.ApplicationRemovedEmailContentProvider;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_REMOVED_NOTIFICATION_TEMPLATE;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicationRemovedEventHandler {
    private final ApplicationRemovedEmailContentProvider contentProvider;
    private final NotificationService notificationService;
    private final CtscTeamLeadLookupConfiguration ctscTeamLeadLookupConfiguration;

    @EventListener
    public void notifyTeamLead(ApplicationRemovedEvent orderEvent) {
        final CaseData caseData = orderEvent.getCaseData();
        final AdditionalApplicationsBundle removedApplication = orderEvent.getRemovedApplication();

        String recipient = ctscTeamLeadLookupConfiguration.getEmail();
        final NotifyData notifyData = contentProvider.getNotifyData(caseData, removedApplication);

        notificationService
            .sendEmail(APPLICATION_REMOVED_NOTIFICATION_TEMPLATE, recipient, notifyData, caseData.getId());
        }
}
