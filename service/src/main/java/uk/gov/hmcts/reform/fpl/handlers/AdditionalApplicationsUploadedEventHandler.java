package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.ApplicantType;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.events.AdditionalApplicationsUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.OrderApplicant;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.Supplement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.CourtService;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_PARTIES_AND_OTHERS;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.UPDATED_INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_CTSC;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AdditionalApplicationsUploadedEventHandler {
    private final RequestData requestData;
    private final NotificationService notificationService;
    private final CourtService courtService;
    private final AdditionalApplicationsUploadedEmailContentProvider contentProvider;
    private final InboxLookupService inboxLookupService;
    private final RepresentativesInbox representativesInbox;
    private final OtherRecipientsInbox otherRecipientsInbox;
    private final RepresentativeNotificationService representativeNotificationService;
    private final SendDocumentService sendDocumentService;
    private final FeatureToggleService featureToggleService;

    @EventListener
    @Async
    public void sendAdditionalApplicationsByPost(final AdditionalApplicationsUploadedEvent event) {
        if (featureToggleService.isServeOrdersAndDocsToOthersEnabled()) {
            final CaseData caseData = event.getCaseData();
            AdditionalApplicationsBundle uploadedBundle = caseData.getAdditionalApplicationsBundle().get(0).getValue();
            final List<DocumentReference> documents = getApplicationDocuments(uploadedBundle);

            Set<Recipient> recipientsToNotify = getRecipientsToNotifyByPost(caseData, uploadedBundle);
            sendDocumentService.sendDocuments(caseData, documents, new ArrayList<>(recipientsToNotify));
        }
    }

    private Set<Recipient> getRecipientsToNotifyByPost(CaseData caseData, AdditionalApplicationsBundle uploadedBundle) {
        Set<Recipient> allRecipients = new LinkedHashSet<>(sendDocumentService.getStandardRecipients(caseData));

        List<Element<Other>> selectedOthers = getOthersSelected(uploadedBundle);
        List<Element<Respondent>> selectedRespondents = getRespondentsSelected(uploadedBundle);

        allRecipients.removeAll(otherRecipientsInbox.getNonSelectedRecipients(
            POST, caseData, selectedOthers, Element::getValue
        ));
        allRecipients.removeAll(representativesInbox.getNonSelectedRespondentRecipientsByPost(
            caseData, selectedRespondents
        ));

        allRecipients.addAll(otherRecipientsInbox.getSelectedRecipientsWithNoRepresentation(selectedOthers));
        allRecipients.addAll(representativesInbox.getSelectedRecipientsWithNoRepresentation(selectedRespondents));

        return allRecipients;
    }

    @EventListener
    @Async
    public void notifyAdmin(final AdditionalApplicationsUploadedEvent event) {
        List<String> roles = new ArrayList<>(requestData.userRoles());
        if (!roles.containsAll(UserRole.HMCTS_ADMIN.getRoleNames())) {
            CaseData caseData = event.getCaseData();

            NotifyData notifyData = contentProvider.getNotifyData(caseData);
            String recipient = courtService.getCourtEmail(caseData);
            String template = featureToggleService.isServeOrdersAndDocsToOthersEnabled()
                              ? UPDATED_INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_CTSC
                              : INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE;

            notificationService.sendEmail(template, recipient, notifyData, caseData.getId());
        }
    }

    @EventListener
    @Async
    public void notifyApplicant(final AdditionalApplicationsUploadedEvent event) {
        if (featureToggleService.isServeOrdersAndDocsToOthersEnabled()) {
            final CaseData caseData = event.getCaseData();
            final OrderApplicant applicant = event.getApplicant();

            if (applicant.getType() == ApplicantType.LOCAL_AUTHORITY) {
                Collection<String> emails = inboxLookupService.getRecipients(
                    LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build());
                sendNotification(caseData, emails);
            } else {
                Map<String, String> emails = getRespondentsEmails(caseData);
                if (isNotEmpty(emails.get(applicant.getName()))) {
                    sendNotification(caseData, Set.of(emails.get(applicant.getName())));
                }
            }
        }
    }

    private Map<String, String> getRespondentsEmails(CaseData caseData) {
        return caseData.getAllRespondents().stream()
            .collect(Collectors.toMap(
                respondent -> respondent.getValue().getParty().getFullName(),
                respondent -> hasNoSolicitorEmail(respondent) ? EMPTY
                                                              : respondent.getValue().getSolicitor().getEmail()
            ));
    }

    private boolean hasNoSolicitorEmail(Element<Respondent> respondent) {
        return isNull(respondent.getValue().getSolicitor()) || isEmpty(respondent.getValue().getSolicitor().getEmail());
    }

    private void sendNotification(CaseData caseData, Collection<String> emails) {
        NotifyData notifyData = contentProvider.getNotifyData(caseData);

        notificationService.sendEmail(
            INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_PARTIES_AND_OTHERS, emails, notifyData,
            caseData.getId().toString()
        );
    }

    @EventListener
    @Async
    public void notifyDigitalRepresentatives(final AdditionalApplicationsUploadedEvent event) {
        if (featureToggleService.isServeOrdersAndDocsToOthersEnabled()) {
            final CaseData caseData = event.getCaseData();
            NotifyData notifyData = contentProvider.getNotifyData(caseData);

            Set<String> digitalRepresentativesEmails = getRepresentativesEmails(caseData, DIGITAL_SERVICE);

            representativeNotificationService.sendNotificationToRepresentatives(
                caseData.getId(),
                notifyData,
                digitalRepresentativesEmails,
                INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_PARTIES_AND_OTHERS
            );
        }
    }

    private Set<String> getRepresentativesEmails(CaseData caseData,
                                                 RepresentativeServingPreferences servingPreference) {
        AdditionalApplicationsBundle uploadedBundle = caseData.getAdditionalApplicationsBundle().get(0).getValue();

        List<Element<Other>> othersSelected = getOthersSelected(uploadedBundle);
        List<Element<Respondent>> respondentsSelected = getRespondentsSelected(uploadedBundle);

        Set<String> digitalRepresentatives = representativesInbox.getEmailsByPreference(caseData, servingPreference);

        Set<String> nonSelectedOthers = otherRecipientsInbox.getNonSelectedRecipients(
            servingPreference, caseData, othersSelected, element -> element.getValue().getEmail()
        );
        digitalRepresentatives.removeAll(nonSelectedOthers);

        Set<String> nonSelectedRespondentsRepresentatives = representativesInbox.getNonSelectedRespondentRecipients(
            servingPreference, caseData, respondentsSelected, element -> element.getValue().getEmail()
        );
        digitalRepresentatives.removeAll(nonSelectedRespondentsRepresentatives);

        return digitalRepresentatives;
    }

    @EventListener
    @Async
    public void notifyEmailServedRepresentatives(final AdditionalApplicationsUploadedEvent event) {
        if (featureToggleService.isServeOrdersAndDocsToOthersEnabled()) {
            final CaseData caseData = event.getCaseData();
            NotifyData notifyData = contentProvider.getNotifyData(caseData);

            Set<String> emailRepresentatives = getRepresentativesEmails(caseData, EMAIL);

            if (!emailRepresentatives.isEmpty()) {
                representativeNotificationService.sendNotificationToRepresentatives(
                    caseData.getId(),
                    notifyData,
                    emailRepresentatives,
                    INTERLOCUTORY_UPLOAD_NOTIFICATION_TEMPLATE_PARTIES_AND_OTHERS
                );
            }
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
        }

        return defaultIfNull(lastBundle.getOtherApplicationsBundle().getOthers(), List.of());
    }

    private List<Element<Respondent>> getRespondentsSelected(final AdditionalApplicationsBundle lastBundle) {
        if (lastBundle.getC2DocumentBundle() != null) {
            return defaultIfNull(lastBundle.getC2DocumentBundle().getRespondents(), List.of());
        }

        return defaultIfNull(lastBundle.getOtherApplicationsBundle().getRespondents(), List.of());
    }
}
