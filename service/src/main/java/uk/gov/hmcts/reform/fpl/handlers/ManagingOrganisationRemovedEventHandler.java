package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.ManagingOrganisationRemoved;
import uk.gov.hmcts.reform.fpl.exceptions.RecipientNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Solicitor;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.ManagingOrganisationRemovedContentProvider;

import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.MANAGING_ORGANISATION_REMOVED_TEMPLATE;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManagingOrganisationRemovedEventHandler {

    private final NotificationService notificationService;
    private final ManagingOrganisationRemovedContentProvider contentProvider;

    @Async
    @EventListener
    public void notifyManagingOrganisation(final ManagingOrganisationRemoved event) {
        CaseData caseData = event.getCaseData();

        String recipient = Optional.ofNullable(caseData.getSolicitor())
            .map(Solicitor::getEmail)
            .filter(StringUtils::isNotBlank)
            .orElseThrow(RecipientNotFoundException::new);

        NotifyData notifyData = contentProvider.getEmailData(event.getManagingOrganisation(), caseData);

        notificationService.sendEmail(MANAGING_ORGANISATION_REMOVED_TEMPLATE, recipient, notifyData, caseData.getId());
    }
}
