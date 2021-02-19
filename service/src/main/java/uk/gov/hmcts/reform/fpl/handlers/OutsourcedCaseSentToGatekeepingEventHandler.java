package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.OutsourcedCaseSentToGatekeepingEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.sendtogatekeeper.NotifyLAOnOutsourcedCaseTemplate;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.OutsourcedCaseSentToGatekeepingContentProvider;

import java.util.Collection;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.OUTSOURCED_CASE_SENT_TO_GATEKEEPING_TEMPLATE;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OutsourcedCaseSentToGatekeepingEventHandler {
    private final NotificationService notificationService;
    private final InboxLookupService inboxLookupService;
    private final OutsourcedCaseSentToGatekeepingContentProvider outsourcedCaseSentToGatekeepingContentProvider;

    @EventListener
    public void notifyManagingLA(OutsourcedCaseSentToGatekeepingEvent event) {
        CaseData caseData = event.getCaseData();

        Collection<String> emails = inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build());

        NotifyLAOnOutsourcedCaseTemplate templateData
            = outsourcedCaseSentToGatekeepingContentProvider.buildNotifyLAOnOutsourcedCaseTemplate(caseData);

        notificationService.sendEmail(
            OUTSOURCED_CASE_SENT_TO_GATEKEEPING_TEMPLATE, emails, templateData, caseData.getId().toString());
    }
}
