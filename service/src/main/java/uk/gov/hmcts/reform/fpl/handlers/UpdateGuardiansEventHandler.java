package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.UpdateGuardianEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.email.content.UpdateGuardianContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;

import java.util.Set;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.GUARDIAN_UPDATED;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UpdateGuardiansEventHandler {
    private final UpdateGuardianContentProvider updateGuardianContentProvider;
    private final LocalAuthorityRecipientsService localAuthorityRecipients;
    private final RepresentativesInbox representativesInbox;
    private final NotificationService notificationService;

    @Async
    @EventListener
    public void notifyLocalAuthorities(final UpdateGuardianEvent event) {
        final CaseData caseData = event.getCaseData();
        final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
            .caseData(caseData)
            .secondaryLocalAuthorityExcluded(true)
            .legalRepresentativesExcluded(true)
            .build();

        final Set<String> recipients = localAuthorityRecipients.getRecipients(recipientsRequest);

        notificationService.sendEmail(GUARDIAN_UPDATED, recipients,
            updateGuardianContentProvider.getUpdateGuardianNotifyData(caseData), caseData.getId());
    }

    @Async
    @EventListener
    public void notifyRespondentSolicitors(final UpdateGuardianEvent event) {
        final CaseData caseData = event.getCaseData();
        final Set<String> recipients = representativesInbox.getRespondentSolicitorEmails(caseData, DIGITAL_SERVICE);
        NotifyData notifyData = updateGuardianContentProvider.getUpdateGuardianNotifyData(caseData);

        notificationService.sendEmail(GUARDIAN_UPDATED, recipients, notifyData, caseData.getId());
    }

    @Async
    @EventListener
    public void notifyChildSolicitors(final UpdateGuardianEvent event) {
        final CaseData caseData = event.getCaseData();
        final Set<String> recipients = representativesInbox.getChildrenSolicitorEmails(caseData, DIGITAL_SERVICE);
        NotifyData notifyData = updateGuardianContentProvider.getUpdateGuardianNotifyData(caseData);

        notificationService.sendEmail(GUARDIAN_UPDATED, recipients, notifyData, caseData.getId());
    }
}
