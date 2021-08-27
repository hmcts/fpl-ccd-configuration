package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.ReturnedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.ReturnedCaseContentProvider;

import java.util.Collection;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_RETURNED_TO_THE_LA;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReturnedCaseEventHandler {
    private final NotificationService notificationService;
    private final LocalAuthorityRecipientsService localAuthorityRecipients;
    private final ReturnedCaseContentProvider returnedCaseContentProvider;

    @EventListener
    public void notifyLocalAuthority(ReturnedCaseEvent event) {

        final CaseData caseData = event.getCaseData();

        final NotifyData notifyData = returnedCaseContentProvider.parametersWithCaseUrl(caseData);

        final RecipientsRequest recipientsRequest = RecipientsRequest.builder().caseData(caseData)
            .legalRepresentativesExcluded(true)
            .secondaryLocalAuthorityExcluded(true)
            .build();

        final Collection<String> recipients = localAuthorityRecipients.getRecipients(recipientsRequest);

        notificationService.sendEmail(APPLICATION_RETURNED_TO_THE_LA, recipients, notifyData, caseData.getId());
    }
}
