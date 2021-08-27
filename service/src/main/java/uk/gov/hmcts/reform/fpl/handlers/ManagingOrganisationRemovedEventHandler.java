package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.ManagingOrganisationRemoved;
import uk.gov.hmcts.reform.fpl.exceptions.RecipientNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.ManagingOrganisationRemovedContentProvider;

import java.util.Set;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.MANAGING_ORGANISATION_REMOVED_TEMPLATE;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManagingOrganisationRemovedEventHandler {

    private final LocalAuthorityRecipientsService localAuthorityRecipients;
    private final NotificationService notificationService;
    private final ManagingOrganisationRemovedContentProvider contentProvider;

    @Async
    @EventListener
    public void notifyManagingOrganisation(final ManagingOrganisationRemoved event) {

        final CaseData caseData = event.getCaseData();

        final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
            .caseData(caseData)
            .legalRepresentativesExcluded(true)
            .secondaryLocalAuthorityExcluded(true)
            .build();

        final Set<String> recipients = localAuthorityRecipients.getRecipients(recipientsRequest);

        if (isEmpty(recipients)) {
            throw new RecipientNotFoundException();
        }

        final NotifyData notifyData = contentProvider.getEmailData(event.getManagingOrganisation(), caseData);

        notificationService.sendEmail(MANAGING_ORGANISATION_REMOVED_TEMPLATE, recipients, notifyData, caseData.getId());
    }
}
