package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.events.AdditionalApplicationsUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.Supplement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.AdditionalApplicationsUploadedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.others.OtherRecipientsInbox;
import uk.gov.hmcts.reform.fpl.service.representative.RepresentativeNotificationService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_PARTIES_AND_OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AdditionalApplicationsUploadedEventHandler {
    private final RequestData requestData;
    private final NotificationService notificationService;
    private final HmctsAdminNotificationHandler adminNotificationHandler;
    private final AdditionalApplicationsUploadedEmailContentProvider additionalApplicationsUploadedEmailContentProvider;
    private final InboxLookupService inboxLookupService;
    private final RepresentativesInbox representativesInbox;
    private final OtherRecipientsInbox otherRecipientsInbox;
    private final RepresentativeNotificationService representativeNotificationService;
    private final SendDocumentService sendDocumentService;
    private final FeatureToggleService featureToggleService;

    @EventListener
    public void notifyParties(final AdditionalApplicationsUploadedEvent event) {
        if (featureToggleService.isServeOrdersAndDocsToOthersEnabled()) {
            final CaseData caseData = event.getCaseData();
            NotifyData notifyData = additionalApplicationsUploadedEmailContentProvider.getNotifyData(caseData);

            AdditionalApplicationsBundle uploadedBundle = caseData.getAdditionalApplicationsBundle().get(0).getValue();
            List<Element<Other>> othersSelected = getOthersSelected(uploadedBundle);
            sendNotificationToLocalAuthorityAndDigitalRepresentatives(caseData, othersSelected, notifyData);
            sendNotificationToEmailServedRepresentatives(caseData, notifyData, othersSelected);
        }
    }

    @EventListener
    @SuppressWarnings("unchecked")
    public void sendAdditionalApplicationsByPost(final AdditionalApplicationsUploadedEvent event) {
        if (featureToggleService.isServeOrdersAndDocsToOthersEnabled()) {
            final CaseData caseData = event.getCaseData();
            AdditionalApplicationsBundle uploadedBundle = caseData.getAdditionalApplicationsBundle().get(0).getValue();
            final List<DocumentReference> documents = getApplicationDocuments(uploadedBundle);

            Set<Recipient> allRecipients = new LinkedHashSet<>(sendDocumentService.getStandardRecipients(caseData));

            List<Element<Other>> othersSelected = getOthersSelected(uploadedBundle);
            Set<Recipient> nonSelectedRecipients = (Set<Recipient>) otherRecipientsInbox.getNonSelectedRecipients(
                POST, caseData, othersSelected, Element::getValue);
            allRecipients.removeAll(nonSelectedRecipients);

            allRecipients.addAll(otherRecipientsInbox.getSelectedRecipientsWithNoRepresentation(othersSelected));
            sendDocumentService.sendDocuments(caseData, documents, new ArrayList<>(allRecipients));
        }
    }

    @EventListener
    public void notifyAdmin(final AdditionalApplicationsUploadedEvent event) {
        List<String> roles = new ArrayList<>(requestData.userRoles());
        if (!roles.containsAll(UserRole.HMCTS_ADMIN.getRoleNames())) {
            CaseData caseData = event.getCaseData();

            NotifyData notifyData = additionalApplicationsUploadedEmailContentProvider
                .getNotifyData(caseData);
            String recipient = adminNotificationHandler.getHmctsAdminEmail(caseData);
            notificationService
                .sendEmail(INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_CTSC, recipient, notifyData, caseData.getId());
        }
    }

    @SuppressWarnings("unchecked")
    private void sendNotificationToLocalAuthorityAndDigitalRepresentatives(final CaseData caseData,
                                                                           List<Element<Other>> othersSelected,
                                                                           NotifyData notifyData) {
        Set<String> digitalRepresentatives = representativesInbox.getEmailsByPreference(caseData, DIGITAL_SERVICE);
        Set<String> digitalRecipientsOtherNotNotified = (Set<String>) otherRecipientsInbox.getNonSelectedRecipients(
            DIGITAL_SERVICE, caseData, othersSelected, element -> element.getValue().getEmail()
        );
        digitalRepresentatives.removeAll(digitalRecipientsOtherNotNotified);

        sendToLocalAuthority(caseData, notifyData);

        representativeNotificationService.sendNotificationToRepresentatives(
            caseData.getId(),
            notifyData,
            digitalRepresentatives,
            INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_PARTIES_AND_OTHERS
        );
    }

    private void sendToLocalAuthority(final CaseData caseData,
                                      final NotifyData notifyData) {
        Collection<String> emails = inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build());

        notificationService.sendEmail(
            INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_PARTIES_AND_OTHERS,
            emails,
            notifyData,
            caseData.getId().toString());
    }

    @SuppressWarnings("unchecked")
    private void sendNotificationToEmailServedRepresentatives(final CaseData caseData,
                                                              final NotifyData notifyData,
                                                              final List<Element<Other>> othersSelected) {
        Set<String> emailRepresentatives = representativesInbox.getEmailsByPreference(caseData, EMAIL);
        Set<String> digitalRecipientsOtherNotNotified = (Set<String>) otherRecipientsInbox.getNonSelectedRecipients(
            EMAIL, caseData, othersSelected, element -> element.getValue().getEmail()
        );
        emailRepresentatives.removeAll(digitalRecipientsOtherNotNotified);

        if (!emailRepresentatives.isEmpty()) {
            representativeNotificationService.sendNotificationToRepresentatives(
                caseData.getId(),
                notifyData,
                emailRepresentatives,
                INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_PARTIES_AND_OTHERS
            );
        }
    }

    private List<DocumentReference> getApplicationDocuments(AdditionalApplicationsBundle bundle) {
        List<DocumentReference> documents = new ArrayList<>();

        if (bundle.getC2DocumentBundle() != null) {
            documents.add(bundle.getC2DocumentBundle().getDocument());
            documents.addAll(
                unwrapElements(bundle.getC2DocumentBundle().getSupplementsBundle())
                    .stream().map(Supplement::getDocument).collect(Collectors.toList()));

            documents.addAll(
                unwrapElements(bundle.getC2DocumentBundle().getSupportingEvidenceBundle())
                    .stream().map(SupportingEvidenceBundle::getDocument).collect(Collectors.toList()));
        }

        if (bundle.getOtherApplicationsBundle() != null) {
            documents.add(bundle.getOtherApplicationsBundle().getDocument());
            documents.addAll(
                unwrapElements(bundle.getOtherApplicationsBundle().getSupplementsBundle())
                    .stream().map(Supplement::getDocument).collect(Collectors.toList()));

            documents.addAll(
                unwrapElements(bundle.getOtherApplicationsBundle().getSupportingEvidenceBundle())
                    .stream().map(SupportingEvidenceBundle::getDocument).collect(Collectors.toList()));
        }

        return documents;
    }

    private List<Element<Other>> getOthersSelected(final AdditionalApplicationsBundle lastBundle) {
        if (lastBundle.getC2DocumentBundle() != null) {
            return defaultIfNull(lastBundle.getC2DocumentBundle().getOthers(), List.of());
        } else if (lastBundle.getOtherApplicationsBundle() != null) {
            return defaultIfNull(lastBundle.getOtherApplicationsBundle().getOthers(), List.of());
        }

        return List.of();
    }
}
