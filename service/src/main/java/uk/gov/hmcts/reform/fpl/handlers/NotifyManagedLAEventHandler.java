package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.events.NotifyManagedLAEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.sendtogatekeeper.ManagedLATemplate;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.ThirdPartyApplicationContentProvider;

import java.util.Collection;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.THIRD_PARTY_SUBMISSION_TEMPLATE;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotifyManagedLAEventHandler {
    private final NotificationService notificationService;
    private final InboxLookupService inboxLookupService;
    private final ThirdPartyApplicationContentProvider thirdPartyApplicationContentProvider;

    @EventListener
    public void notifyManagedLA(NotifyManagedLAEvent event) {
        CaseData caseData = event.getCaseData();

        Collection<String> emails = inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build());

        ManagedLATemplate parameters = thirdPartyApplicationContentProvider.buildManagedLANotification(caseData);
        notificationService.sendEmail(THIRD_PARTY_SUBMISSION_TEMPLATE, emails, parameters, caseData.getId().toString());
    }
}
