package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.CtscTeamLeadLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.ApplicationFormRemovedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.ApplicationFormRemovedEmailContentProvider;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_FORM_REMOVED_CTSC_LEAD_TEMPLATE;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicationFormRemovedEventHandler {
    private final ApplicationFormRemovedEmailContentProvider contentProvider;
    private final NotificationService notificationService;
    private final CtscTeamLeadLookupConfiguration ctscTeamLeadLookupConfiguration;

    @EventListener
    public void notifyTeamLead(ApplicationFormRemovedEvent event) {
        final CaseData caseData = event.getCaseData();
        final DocumentReference removedApplicationForm = event.getRemovedApplicationForm();

        String recipient = ctscTeamLeadLookupConfiguration.getEmail();
        final NotifyData notifyData = contentProvider.getNotifyData(caseData);

        notificationService
            .sendEmail(APPLICATION_FORM_REMOVED_CTSC_LEAD_TEMPLATE, recipient, notifyData, caseData.getId());
    }
}
