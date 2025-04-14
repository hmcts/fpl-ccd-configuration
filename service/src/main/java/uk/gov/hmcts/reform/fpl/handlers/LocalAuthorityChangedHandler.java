package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.HighCourtAdminEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.CaseTransferred;
import uk.gov.hmcts.reform.fpl.events.CaseTransferredToAnotherCourt;
import uk.gov.hmcts.reform.fpl.events.SecondaryLocalAuthorityAdded;
import uk.gov.hmcts.reform.fpl.events.SecondaryLocalAuthorityRemoved;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.LocalAuthorityChangedContentProvider;

import java.util.Set;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CASE_TRANSFERRED_NEW_DESIGNATED_LA_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CASE_TRANSFERRED_PREV_DESIGNATED_LA_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CASE_TRANSFERRED_TO_ANOTHER_COURT_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LOCAL_AUTHORITY_ADDED_DESIGNATED_LA_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LOCAL_AUTHORITY_ADDED_SHARED_LA_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.LOCAL_AUTHORITY_REMOVED_SHARED_LA_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LocalAuthorityChangedHandler {

    private final LocalAuthorityRecipientsService localAuthorityRecipients;

    private final NotificationService notificationService;

    private final LocalAuthorityChangedContentProvider contentProvider;

    private final RepresentativesInbox representativesInbox;

    private final CourtService courtService;

    private final HighCourtAdminEmailLookupConfiguration highCourtAdminEmailLookupConfiguration;

    @Async
    @EventListener
    public void notifySecondaryLocalAuthority(final SecondaryLocalAuthorityRemoved event) {

        final CaseData caseData = event.getCaseData();
        final CaseData caseDataBefore = event.getCaseDataBefore();

        final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
            .caseData(caseDataBefore)
            .designatedLocalAuthorityExcluded(true)
            .legalRepresentativesExcluded(true)
            .build();

        final Set<String> recipients = localAuthorityRecipients.getRecipients(recipientsRequest);

        final NotifyData notifyData = contentProvider.getNotifyDataForRemovedLocalAuthority(caseData, caseDataBefore);

        notificationService.sendEmail(LOCAL_AUTHORITY_REMOVED_SHARED_LA_TEMPLATE, recipients, notifyData,
            caseData.getId());
    }

    @Async
    @EventListener
    public void notifySecondaryLocalAuthority(final CaseTransferredToAnotherCourt event) {
        final CaseData caseData = event.getCaseData();

        final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
            .caseData(caseData)
            .designatedLocalAuthorityExcluded(true)
            .legalRepresentativesExcluded(true)
            .build();

        final Set<String> recipients = localAuthorityRecipients.getRecipients(recipientsRequest);
        final NotifyData notifyData = contentProvider.getNotifyDataFoTransferredToAnotherCourt(caseData);
        notificationService.sendEmail(CASE_TRANSFERRED_TO_ANOTHER_COURT_TEMPLATE, recipients, notifyData,
            caseData.getId());
    }

    @Async
    @EventListener
    public void notifySecondaryLocalAuthority(final SecondaryLocalAuthorityAdded event) {

        final CaseData caseData = event.getCaseData();

        final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
            .caseData(caseData)
            .designatedLocalAuthorityExcluded(true)
            .legalRepresentativesExcluded(true)
            .build();

        final Set<String> recipients = localAuthorityRecipients.getRecipients(recipientsRequest);

        final NotifyData notifyData = contentProvider.getNotifyDataForAddedLocalAuthority(caseData);

        notificationService.sendEmail(LOCAL_AUTHORITY_ADDED_SHARED_LA_TEMPLATE, recipients, notifyData,
            caseData.getId());
    }

    @Async
    @EventListener
    public void notifyDesignatedLocalAuthority(final CaseTransferredToAnotherCourt event) {
        final CaseData caseData = event.getCaseData();

        final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
            .caseData(caseData)
            .secondaryLocalAuthorityExcluded(true)
            .legalRepresentativesExcluded(true)
            .build();

        final Set<String> recipients = localAuthorityRecipients.getRecipients(recipientsRequest);
        if (!recipients.isEmpty()) {
            final NotifyData notifyData = contentProvider.getNotifyDataFoTransferredToAnotherCourt(caseData);
            notificationService.sendEmail(CASE_TRANSFERRED_TO_ANOTHER_COURT_TEMPLATE, recipients, notifyData,
                caseData.getId());
        }
    }

    @Async
    @EventListener
    public void notifyDesignatedLocalAuthority(final SecondaryLocalAuthorityAdded event) {

        final CaseData caseData = event.getCaseData();

        final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
            .caseData(caseData)
            .secondaryLocalAuthorityExcluded(true)
            .legalRepresentativesExcluded(true)
            .build();

        final Set<String> recipients = localAuthorityRecipients.getRecipients(recipientsRequest);

        final NotifyData notifyData = contentProvider.getNotifyDataForDesignatedLocalAuthority(caseData);

        notificationService.sendEmail(LOCAL_AUTHORITY_ADDED_DESIGNATED_LA_TEMPLATE, recipients, notifyData,
            caseData.getId());
    }

    @Async
    @EventListener
    public void notifyNewDesignatedLocalAuthority(final CaseTransferred event) {

        final CaseData caseData = event.getCaseData();
        final CaseData caseDataBefore = event.getCaseDataBefore();

        final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
            .caseData(caseData)
            .secondaryLocalAuthorityExcluded(true)
            .legalRepresentativesExcluded(true)
            .build();

        final Set<String> recipients = localAuthorityRecipients.getRecipients(recipientsRequest);

        final NotifyData notifyData = contentProvider.getCaseTransferredNotifyData(caseData, caseDataBefore);

        notificationService.sendEmail(CASE_TRANSFERRED_NEW_DESIGNATED_LA_TEMPLATE, recipients, notifyData,
            caseData.getId());
    }

    @Async
    @EventListener
    public void notifyPreviousDesignatedLocalAuthority(final CaseTransferred event) {

        final CaseData caseDataBefore = event.getCaseDataBefore();
        final CaseData caseData = event.getCaseData();

        final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
            .caseData(caseDataBefore)
            .secondaryLocalAuthorityExcluded(true)
            .legalRepresentativesExcluded(true)
            .build();

        final Set<String> recipients = localAuthorityRecipients.getRecipients(recipientsRequest);

        final NotifyData notifyData = contentProvider.getCaseTransferredNotifyData(caseData, caseDataBefore);

        notificationService.sendEmail(CASE_TRANSFERRED_PREV_DESIGNATED_LA_TEMPLATE, recipients,
            notifyData, caseData.getId());
    }

    @Async
    @EventListener
    public void notifyChildSolicitors(final CaseTransferredToAnotherCourt event) {
        final CaseData caseData = event.getCaseData();
        final Set<String> recipients = representativesInbox.getChildrenSolicitorEmails(caseData, DIGITAL_SERVICE);
        if (!recipients.isEmpty()) {
            final NotifyData notifyData = contentProvider.getNotifyDataFoTransferredToAnotherCourt(caseData);
            notificationService.sendEmail(CASE_TRANSFERRED_TO_ANOTHER_COURT_TEMPLATE, recipients, notifyData,
                caseData.getId());
        }
    }

    @Async
    @EventListener
    public void notifyRespondentSolicitors(final CaseTransferredToAnotherCourt event) {
        final CaseData caseData = event.getCaseData();
        final Set<String> recipients = representativesInbox.getRespondentSolicitorEmails(caseData, DIGITAL_SERVICE);
        if (!recipients.isEmpty()) {
            final NotifyData notifyData = contentProvider.getNotifyDataFoTransferredToAnotherCourt(caseData);
            notificationService.sendEmail(CASE_TRANSFERRED_TO_ANOTHER_COURT_TEMPLATE, recipients, notifyData,
                caseData.getId());
        }
    }

    @Async
    @EventListener
    public void notifyHighCourtAdmin(final CaseTransferredToAnotherCourt event) {
        final CaseData caseData = event.getCaseData();
        final String recipient = highCourtAdminEmailLookupConfiguration.getEmail();
        if (courtService.isHighCourtCase(caseData)) {
            final NotifyData notifyData = contentProvider.getNotifyDataFoTransferredToAnotherCourt(caseData);
            notificationService.sendEmail(CASE_TRANSFERRED_TO_ANOTHER_COURT_TEMPLATE, recipient, notifyData,
                caseData.getId());
        }
    }
}
