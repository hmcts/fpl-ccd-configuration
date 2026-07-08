package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.ApplicantsDetailsUpdatedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.ApplicantsDetailsUpdatedContentProvider;

import java.util.Set;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICANTS_DETAILS_UPDATED;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicantsDetailsUpdatedEventHandler {
    private final ApplicantsDetailsUpdatedContentProvider contentProvider;
    private final LocalAuthorityRecipientsService localAuthorityRecipients;
    private final NotificationService notificationService;

    @Async
    @EventListener
    void notifyLocalAuthorities(final ApplicantsDetailsUpdatedEvent event) {
        final CaseData caseData = event.getCaseData();
        final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
            .caseData(caseData)
            .secondaryLocalAuthorityExcluded(true)
            .legalRepresentativesExcluded(true)
            .build();

        final Set<String> recipients = localAuthorityRecipients.getRecipients(recipientsRequest);

        notificationService.sendEmail(APPLICANTS_DETAILS_UPDATED, recipients,
            contentProvider.getApplicantsDetailsUpdatedNotifyData(caseData), caseData.getId());
    }
}
