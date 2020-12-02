package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.ReturnedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.ReturnedCaseContentProvider;

import java.util.Collection;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_RETURNED_TO_THE_LA;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReturnedCaseEventHandler {
    private final NotificationService notificationService;
    private final InboxLookupService inboxLookupService;
    private final ReturnedCaseContentProvider returnedCaseContentProvider;

    @EventListener
    public void notifyLocalAuthority(ReturnedCaseEvent event) {
        CaseData caseData = event.getCaseData();

        NotifyData notifyData = returnedCaseContentProvider.parametersWithCaseUrl(caseData);
        Collection<String> emails = inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData)
                .excludeLegalRepresentatives(true)
                .build()
        );

        notificationService.sendEmail(APPLICATION_RETURNED_TO_THE_LA, emails, notifyData, caseData.getId().toString());
    }
}
